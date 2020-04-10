package com.fzu.recommend.service;

import com.fzu.recommend.entity.User;
import com.fzu.recommend.util.RecommendConstant;
import com.fzu.recommend.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class FollowService implements RecommendConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    //关注
    public void follow(int userId, int entityType, int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                //判断是否已经关注
                boolean hasFollowed = redisOperations.opsForZSet().score(followeeKey, entityId) != null;
                redisOperations.multi();
                if(hasFollowed){ //已关注，取消关注
                    //从当前用户的关注列表中移除实体
                    redisOperations.opsForZSet().remove(followeeKey, entityId);
                    //从实体对象的粉丝列表中移除当前用户
                    redisOperations.opsForZSet().remove(followerKey, userId);
                }else{ //未关注
                    redisOperations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                    redisOperations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());
                }
                return redisOperations.exec();
            }
        });
    }

    //查询关注的实体的数量
    public long findFolloweeCount(int userId, int entityType){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    //查询实体的粉丝数量
    public long findFollowerCount(int entityType, int entityId){
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    //查询当前用户是否关注某实体
    public boolean hasFollowed(int userId, int entityType, int entityId){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    //查询用户的关注的人
    public List<User> findFolloweeList(int userId, int offset, int limit){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        List<User> list =  new ArrayList<>();
        if(targetIds != null){
            for(Integer targetId : targetIds) {
                list.add(userService.findUserById(targetId));
            }
        }
        return list;
    }

    //查询实体的粉丝列表
    public List<User> findFollowerList(int userId, int offset, int limit){
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER,userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        List<User> list =  new ArrayList<>();
        if(targetIds != null){
            for(Integer targetId : targetIds) {
                list.add(userService.findUserById(targetId));
            }
        }
        return list;
    }
}
