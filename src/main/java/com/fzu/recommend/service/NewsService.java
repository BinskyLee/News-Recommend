package com.fzu.recommend.service;

import com.fzu.recommend.dao.NewsMapper;
import com.fzu.recommend.entity.News;
import com.fzu.recommend.util.RecommendConstant;
import com.fzu.recommend.util.RecommendUtil;
import org.ansj.app.keyword.KeyWordComputer;
import org.ansj.app.keyword.Keyword;
import org.ansj.recognition.impl.StopRecognition;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class NewsService implements RecommendConstant {

    private static final Logger logger = LoggerFactory.getLogger(NewsService.class);

    private static final Date start;

    static{
        try {
            start = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2020-1-21 00:00:00");
        } catch (ParseException e) {
            throw  new RuntimeException("初始化起始日失败" + e.getMessage());
        }
    }

    @Autowired
    private NewsMapper newsMapper;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private DataService dataService;


    public List<News> findNews(int userId, int offset, int limit, int categoryId, int sort){
        return newsMapper.selectNews(userId, offset, limit, categoryId, sort);
    }

    public int findNewsRows(int userId, int categoryId){
        return newsMapper.selectNewsRows(userId, categoryId);
    }

    public Map<String, Object> addNews(News news){
        Map<String, Object> map = new HashMap<>();
        if(news == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        if(StringUtils.isBlank(news.getTitle())){
            map.put("msg", "标题不能为空");
            return map;
        }
        if(StringUtils.isBlank(news.getContent())){
            map.put("msg", "正文不能为空");
            return map;
        }
        //转义HTML标记
//        news.setTitle(HtmlUtils.htmlEscape(news.getTitle()));
//        news.setContent(HtmlUtils.htmlEscape(news.getContent()));
        newsMapper.insertNews(news);
        return null;
    }


    public News findNewsById(int id){
        return newsMapper.selectNewsById(id);
    }

    public int updateCommentCount(int id, int commentCount){
        return newsMapper.updateCommentCount(id, commentCount);
    }

    public int updateContent(int id, String content){
        return newsMapper.updateContent(id, content);
    }

    public int updateKeywords(int newsId, String keywords){
        return newsMapper.updateKeywords(newsId, keywords);
    }

    public String getKeywords(News news){
        //提取标题的3个关键词
        KeyWordComputer keyWordComputer = new KeyWordComputer(3);
        news.setContent(RecommendUtil.htmlReplace(news.getContent()));
        news.setTitle(RecommendUtil.stringFilter(news.getTitle()));
        news.setContent(RecommendUtil.stringFilter(news.getContent()));
        Collection<Keyword> result = keyWordComputer.computeArticleTfidf(news.getTitle(), news.getContent());
        StringBuilder sb = new StringBuilder();
        for(Keyword keyword: result){
            sb.append(keyword.getName() + ",");
        }
        return sb.toString();
    }

    public int updateScore(int id, double score){
        return newsMapper.updateScore(id, score);
    }

    public int updateClickCount(int id, int clickCount){
        return newsMapper.updateClickCount(id, clickCount);
    }

    public void refreshScore(int newsId){
        News news = newsMapper.selectNewsById(newsId);

        if(news == null){
            logger.error("该文章不存在， id = " + news.getId());
            return;
        }
        long viewCount = dataService.getViewCount(news.getId(), news.getClickCount());
        int commentCount = news.getCommentCount();
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_NEWS, newsId);
        long favoriteCount = favoriteService.findNewsFavoriteCount(newsId);

        // 计算权重
        double score = Math.log10(1 + Math.log10(viewCount + 1) * 4
                + commentCount * 5 + likeCount + favoriteCount * 3)
                + (double)(news.getCreateTime().getTime() - start.getTime()) / (double)(1000 * 3600 * 24);
        // 更新新闻分数
        newsMapper.updateScore(newsId, score);
        // 同步搜索数据
        news.setScore(score);
        elasticsearchService.saveNews(news);
    }




}
