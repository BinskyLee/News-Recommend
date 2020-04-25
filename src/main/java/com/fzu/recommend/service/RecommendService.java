package com.fzu.recommend.service;


import com.fzu.recommend.entity.News;
import com.fzu.recommend.recommend.UserSimilarity;
import com.fzu.recommend.recommend.UserTag;
import com.fzu.recommend.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class RecommendService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private NewsService newsService;

    @Autowired
    private DataService dataService;

    @Autowired
    private UserSimilarity userSimilarity;

    @Autowired
    private UserTag userTag;

    // 计算新闻相似度
    public void computeNewsSimilarity(News news1,News news2){
        if(StringUtils.isBlank(news1.getKeywords()) || StringUtils.isBlank(news2.getKeywords())){
            return;
        }
        Set<String> keywords1 =new HashSet<>(Arrays.asList(news1.getKeywords().split(",")));
        Set<String> keywords2 = new HashSet<>(Arrays.asList(news2.getKeywords().split(",")));
        Set<String> union = new HashSet<>();
        Set<String> inter = new HashSet<>();
        union.addAll(keywords1);
        union.addAll(keywords2);
        inter.addAll(keywords1);
        inter.retainAll(keywords2);
        if(inter.size() == 0){
            return;
        }
        double col = (double)inter.size() / (double)union.size();
        String redisKey1 = RedisKeyUtil.getNewsSimKey(news1.getId());
        String redisKey2 = RedisKeyUtil.getNewsSimKey(news2.getId());
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                redisOperations.multi();
                redisOperations.opsForZSet().add(redisKey1, news2.getId(), col);
                redisOperations.opsForZSet().add(redisKey2, news1.getId(), col);
                return redisOperations.exec();
            }
        });
        // 删除多余部分， 只保留相似度最高的5个
        if(redisTemplate.opsForZSet().zCard(redisKey1) > 5){
            redisTemplate.opsForZSet().removeRange(redisKey1, 0, redisTemplate.opsForZSet().size(redisKey1) - 6);
        }
        if(redisTemplate.opsForZSet().zCard(redisKey2) > 5){
            redisTemplate.opsForZSet().removeRange(redisKey2, 5, redisTemplate.opsForZSet().size(redisKey2) - 6);
        }

    }

    // 基于内容推荐
    public List<News> contentBasedRecommend(int newsId){
        String redisKey = RedisKeyUtil.getNewsSimKey(newsId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(redisKey, 0, 4);
        List<News> list = new ArrayList<>();
        if(targetIds != null){
            for(Integer targetId : targetIds){
                News news = newsService.findNewsById(targetId);
                news.setClickCount((int)dataService.getViewCount(news.getId(), news.getClickCount()));
                list.add(news);
            }
        }
        return list;
    }

    // 混合推荐
    public List<News> getRecommendNews(int userId, int offset, int limit){
        String redisKey = RedisKeyUtil.getRecommendKey(userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(redisKey, offset, offset + limit - 1);
        List<News> list = new ArrayList<>();
        for(Integer id : targetIds){
            list.add(newsService.findNewsById(id));
        }
        return list;
    }

    public long getRecommendRows(int userId){
        String redisKey = RedisKeyUtil.getRecommendKey(userId);
        return redisTemplate.opsForZSet().size(redisKey);
    }

    public void calUserSim(){
        userSimilarity.calSimilarity();;
    }

    public void calUserTag(){
        userTag.calUserTag();;
    }

    // 获取关注用户动态
    public List<News> getNewsFeeds(int userId, int offset, int limit){
        String redisKey = RedisKeyUtil.getFeedsKey(userId);
        List<Integer> targetIds = redisTemplate.opsForList().range(redisKey, offset, offset + limit - 1);
        List<News> list = new ArrayList<>();
        for(Integer id : targetIds){
            News news = newsService.findNewsById(id);
            list.add(news);
        }
        return list;
    }

    public long getFeedsCount(int userId){
        String redisKey = RedisKeyUtil.getFeedsKey(userId);
        return redisTemplate.opsForList().size(redisKey);
    }
}
