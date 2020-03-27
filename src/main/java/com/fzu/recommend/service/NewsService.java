package com.fzu.recommend.service;

import com.fzu.recommend.dao.NewsMapper;
import com.fzu.recommend.entity.News;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

}
