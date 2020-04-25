package com.fzu.recommend.recommend;

import com.fzu.recommend.entity.ActionLog;
import com.fzu.recommend.entity.News;
import com.fzu.recommend.service.ActionLogService;
import com.fzu.recommend.service.NewsService;
import com.fzu.recommend.util.EncodeUtil;
import com.fzu.recommend.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Component
public class UserTag {

    private static final Logger logger = LoggerFactory.getLogger(UserTag.class);

    // 计算用户标签
    // 所有有行为的用户
    private List<Integer> ids = new ArrayList<>();  // 所有有行为的用户

    @Autowired
    private ActionLogService actionLogService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private NewsService newsService;

    public void calUserTag(){

        // 删除关键字
        String redisKey = RedisKeyUtil.getTagKey(null) + "(";
        redisTemplate.delete(redisKey);


        // 获取近期操作过的用户存入ids中
        ids = actionLogService.getUserIds();

        for(int id : ids){
            List<ActionLog> logs = actionLogService.getUserLog(id);
            Map<Integer, Double> newsMap = new HashMap<>();
            for(ActionLog log : logs){
                // 计算对新闻的权值
                double weight = log.getActionType() * timeFunction(new Date(), log.getActionTime());
                double sum = newsMap.get(log.getNewsId()) == null ? 0 : newsMap.get(log.getNewsId());
                newsMap.put(log.getNewsId(), sum + weight);
            }
            // 对用户文章权值进行转换， 将权值分配给关键字
            Map<String, Double> tagMap = new HashMap<>();
            Set<Map.Entry<Integer, Double>> entrySet = newsMap.entrySet();
            for(Map.Entry<Integer, Double> entry : entrySet){
                int newsId = entry.getKey();
                News news = newsService.findNewsById(newsId);
                String[] keywords = news.getKeywords().split(",");
                for(String keyword : keywords){
                    double weight = tagMap.get(keyword) == null ? 0 : tagMap.get(keyword);
                    tagMap.put(keyword, weight + entry.getValue());
                }
            }
            // 对HashMap排序
            List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(tagMap.entrySet());
            Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
                // 降序排序
                @Override
                public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });
            list = list.subList(0, 10);
            for(Map.Entry<String, Double> map :list){
                // 加入keyword的推荐用户列表
                try {
                    System.out.println(map.getKey());
                    String keyword = EncodeUtil.encode(map.getKey(), "UTF-8");
                    redisKey = RedisKeyUtil.getTagKey(keyword);
                    redisTemplate.opsForSet().add(redisKey, id);
                } catch (UnsupportedEncodingException e) {
                    logger.error("汉字编码失败:" + e.getMessage());
                }
            }
        }
    }

    private double timeFunction(Date t1, Date t2){
        return (1.0 / (1.0 + (double)Math.abs(t1.getTime() - t2.getTime()) / (double)(1000 * 3600 * 24 * 7)));
    }
}
