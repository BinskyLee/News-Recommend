package com.fzu.recommend.recommend;


import com.fzu.recommend.entity.ActionLog;
import com.fzu.recommend.service.ActionLogService;
import com.fzu.recommend.util.RecommendUtil;
import com.fzu.recommend.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;


@Component
public class UserSimilarity {

    private Map<Integer, Map<Integer, Date>> userFeeds = new HashMap<>(); // 用户操作过的新闻及操作时间

    private Map<Integer, Map<Integer, Double>> userSim = new HashMap<>();

    private List<Integer> userLogIds = new ArrayList<>();

    private List<Integer> newsLogIds = new ArrayList<>();

    private Set<UserToUser> userRelation = new HashSet<>();

    private Map<Integer, List<Integer>> newsVisited = new HashMap<>();

    @Autowired
    private ActionLogService actionLogService;

    @Autowired
    private RedisTemplate redisTemplate;


    public void calSimilarity(){

        // 获取所有操作过的用户存入userLogIds中
        userLogIds = actionLogService.getUserIds();

        // 遍历userLogIds，获取用户读过的所有文章集合，将新闻Id和最后操作时间存入userFeeds中
        for(Integer userLogId : userLogIds){
            List<ActionLog> logs = actionLogService.getLatestLog(userLogId);
            Map<Integer, Date> map = new HashMap<>();
            for(ActionLog log : logs) {
                map.put(log.getNewsId(), log.getActionTime());
            }
            userFeeds.put(userLogId, map);
        }

        // 获取行为日志中所有的新闻编号存入newsLogIds中
        newsLogIds = actionLogService.getNewsIds();
        for(Integer newsLogId : newsLogIds){
            // 获取阅读过该新闻的用户集合
            List<Integer> ids = actionLogService.getReadUserIds(newsLogId);
            newsVisited.put(newsLogId, ids);
            // 给阅读同一篇文章的用户建立联系
            UserToUser utu = new UserToUser();
            for(int i = 0; i < ids.size(); i++){
                int id1 = ids.get(i);
                for(int j = i + 1; j < ids.size(); j++){
                    int id2 = ids.get(j);
                    utu.setUserId1(id1);
                    utu.setUserId2(id2);
                    userRelation.add(utu); // 加入关联用户集
                }
            }
        }

        // 计算关联用户的相似度
        for(UserToUser utu : userRelation){
            int id1 = utu.getUserId1();
            int id2 = utu.getUserId2();
            double sim = 0.0;
            Map<Integer, Date> feeds1 = userFeeds.get(id1);
            Map<Integer, Date> feeds2 = userFeeds.get(id2);
            Set<Integer> newsSet1 = feeds1.keySet();
            Set<Integer> newsSet2 = feeds2.keySet();
            // 用户浏览总数 |N(u)∪N(v)|
            int total = RecommendUtil.union(newsSet1, newsSet2).size();
            // 用户浏览交集 |N(u)∩N(v)|
            Set<Integer> ins = RecommendUtil.intersection(newsSet1, newsSet2);
            // 获取用户对交集中每条新闻的操作
            for(Integer newsId : ins){
                Date t1 = userFeeds.get(id1).get(newsId);
                Date t2 = userFeeds.get(id2).get(newsId);
                sim += (1.0 / Math.log10(1.0 + newsVisited.get(newsId).size()) * timeFunction(t1, t2)) / total;
            }
            Map<Integer, Double> map =  new HashMap<>();
            map.put(id2, sim);
            userSim.put(id1, map);
            map = new HashMap<>();
            map.put(id1, sim);
            userSim.put(id2, map);
        }
        // 将数据存如redis中
        for(Integer userId : userLogIds){
            String redisKey = RedisKeyUtil.getUserSimKey(userId);
            // 清空key
            redisTemplate.delete(redisKey);
            Map<Integer, Double> map = userSim.get(userId);
            Set<Map.Entry<Integer, Double>> entrySet = map.entrySet();
            for(Map.Entry<Integer, Double> entry : entrySet){
                int id = entry.getKey();
                double sim = entry.getValue();
                redisTemplate.opsForZSet().add(redisKey, id, sim);
            }
            // 只保存至多10个相似用户
            if(redisTemplate.opsForZSet().size(redisKey) > 10){
                redisTemplate.opsForZSet().removeRange(redisKey, 0, redisTemplate.opsForZSet().size(redisKey) - 11);
            }
        }
    }

    private double timeFunction(Date t1, Date t2){
        return (1.0 / (1.0 + (double)Math.abs(t1.getTime() - t2.getTime()) / (double)(1000 * 3600 * 24 * 7)));
    }



}
