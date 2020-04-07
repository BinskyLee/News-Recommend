package com.fzu.recommend.service;

import com.fzu.recommend.dao.NewsMapper;
import com.fzu.recommend.entity.News;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NewsService {

    @Autowired
    private NewsMapper newsMapper;

    public List<News> findNews(int userId, int offset, int limit){
        return newsMapper.selectNews(userId, offset, limit);
    }

    public int findNewsRows(int userId){
        return newsMapper.selectNewsRows(userId);
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



}
