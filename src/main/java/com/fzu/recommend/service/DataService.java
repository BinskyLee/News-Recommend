package com.fzu.recommend.service;

import com.fzu.recommend.entity.News;
import com.fzu.recommend.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class DataService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private NewsService newsService;


    //浏览记录
    public void recordUV(int newsId, String ip){
        String redisKey = RedisKeyUtil.getViewKey(newsId);
        redisTemplate.opsForHyperLogLog().add(redisKey, ip);
    }

    //获取浏览总数
    public long getViewCount(int newsId, int clickCount){
        String redisKey = RedisKeyUtil.getViewKey(newsId);
        return (redisTemplate.opsForHyperLogLog().size(redisKey) + clickCount);
    }



}
