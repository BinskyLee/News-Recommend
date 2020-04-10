package com.fzu.recommend.service;

import com.fzu.recommend.entity.News;
import com.fzu.recommend.util.RecommendUtil;
import com.fzu.recommend.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FavoriteService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private NewsService newsService;

    //收藏
    public void favorite(int userId, int newsId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String newsFavoriteKey = RedisKeyUtil.getNewsFavoriteKey(newsId);
                String userFavoriteKey = RedisKeyUtil.getUserFavoriteKey(userId);
                //该当前用户是否已收藏该新闻
                boolean hasFav = operations.opsForZSet().score(userFavoriteKey, newsId) != null;
                operations.multi(); //开始事务
                if(hasFav){
                    //取消收藏
                    operations.opsForZSet().remove(userFavoriteKey, newsId);
                    operations.opsForValue().decrement(newsFavoriteKey);
                }else{
                    //添加收藏
                    operations.opsForZSet().add(userFavoriteKey, newsId, System.currentTimeMillis());
                    operations.opsForValue().increment(newsFavoriteKey);
                }
                return operations.exec();
            }
        });
    }

    //新闻收藏数
    public int findNewsFavoriteCount(int newsId){
        String newsFavoriteKey = RedisKeyUtil.getNewsFavoriteKey(newsId);
        Integer count = (Integer) redisTemplate.opsForValue().get(newsFavoriteKey);
        return count == null ? 0 : count.intValue();
    }

    //用户收藏数
    public long findUserFavoriteCount(int userId){
        String userFavoriteKey = RedisKeyUtil.getUserFavoriteKey(userId);
        return redisTemplate.opsForZSet().zCard(userFavoriteKey);
    }

    //收藏状态
    public boolean findUserFavoriteStatus(int userId, int newsId){
        String userFavoriteKey = RedisKeyUtil.getUserFavoriteKey(userId);
        return redisTemplate.opsForZSet().score(userFavoriteKey, newsId) != null;
    }

    //收藏新闻列表
    public List<Map<String, Object>> findFavoriteNews(int userId, int offset, int limit){
        String userFavoriteKey = RedisKeyUtil.getUserFavoriteKey(userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(userFavoriteKey, offset, offset + limit - 1);
        if(targetIds == null){
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for(Integer targetId : targetIds){
            Map<String, Object> map = new HashMap<>();
            News news = newsService.findNewsById(targetId);
            news.setContent(RecommendUtil.htmlReplace(news.getContent()));
            map.put("news", news);
            Double score = redisTemplate.opsForZSet().score(userFavoriteKey, targetId);
            map.put("favTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }
}
