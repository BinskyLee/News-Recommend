package com.fzu.recommend.service;


import com.fzu.recommend.entity.News;
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
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String redisKey1 = RedisKeyUtil.getSimilarityKey(news1.getId());
                String redisKey2 = RedisKeyUtil.getSimilarityKey(news2.getId());
                redisOperations.multi();
                redisOperations.opsForZSet().add(redisKey1, news2.getId(), col);
                redisOperations.opsForZSet().add(redisKey2, news1.getId(), col);
                return redisOperations.exec();
            }
        });
    }

    // 基于内容推荐
    public List<News> contentBasedRecommend(int newsId){
        String redisKey = RedisKeyUtil.getSimilarityKey(newsId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(redisKey, 0, 9);
        List<News> list = new ArrayList<>();

        if(targetIds != null){
            for(Integer targetId : targetIds){
                News news = newsService.findNewsById(targetId);
                list.add(news);
            }
        }
        return list;
    }
}
