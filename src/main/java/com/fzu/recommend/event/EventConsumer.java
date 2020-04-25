package com.fzu.recommend.event;

import com.alibaba.fastjson.JSONObject;
import com.fzu.recommend.entity.ActionLog;
import com.fzu.recommend.entity.Event;
import com.fzu.recommend.entity.Message;
import com.fzu.recommend.entity.News;
import com.fzu.recommend.service.*;
import com.fzu.recommend.util.EncodeUtil;
import com.fzu.recommend.util.RecommendConstant;
import com.fzu.recommend.util.RecommendUtil;
import com.fzu.recommend.util.RedisKeyUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.*;

@Component
public class EventConsumer implements RecommendConstant {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private NewsService newsService;

    @Autowired
    private DataService dataService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ActionLogService actionLogService;

    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})
    public void handleMessage(ConsumerRecord record){
        if(record == null || record.value() == null){
            logger.error("消息的内容为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event == null){
            logger.error("消息格式错误");
            return;
        }
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());
        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());
        if(!event.getData().isEmpty()){
            for(Map.Entry<String, Object> entry : event.getData().entrySet()){
                content.put(entry.getKey(), entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }

    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record){
        if(record == null || record.value() == null){
            logger.error("消息的内容为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event == null){
            logger.error("消息格式错误");
            return;
        }
        News news = newsService.findNewsById(event.getEntityId());
        news.setContent(RecommendUtil.htmlReplace(news.getContent()));
        elasticsearchService.saveNews(news); //更新索引
        if((int)event.getData().get("publish") == ENTITY_TYPE_NEWS){
            // 获取关键字
            String keywords = newsService.getKeywords(news);
            newsService.updateKeywords(news.getId(), keywords);
            // 加入近期操作新闻集合
            String redisKey = RedisKeyUtil.getActionNewsKey();
            redisTemplate.opsForSet().add(redisKey, news.getId());
            // 推送给标签下的用户
            String[] tags = keywords.split(",");
            for(String tag : tags){
                // 推送给该标签下用户
                try {
                    tag = EncodeUtil.encode(tag, "UTF-8");
                    redisKey = RedisKeyUtil.getTagKey(tag);
                    Set<Integer> ids = redisTemplate.opsForSet().members(redisKey);
                    for(Integer id : ids){
                        redisKey = RedisKeyUtil.getRecommendKey(id);
                        redisTemplate.opsForZSet().add(redisKey, news.getId(), new Date().getTime());
                        // 删除多余部分
                        if(redisTemplate.opsForZSet().size(redisKey) > 100){
                            redisTemplate.opsForZSet().removeRange(redisKey, 0, 0);
                        }
                    }
                } catch (UnsupportedEncodingException e) {
                    logger.error("汉字转换失败:" + e.getMessage());
                }
            }

            // 推送给关注用户
            String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, event.getUserId());
            Set<Integer> targetIds = redisTemplate.opsForZSet().range(followerKey, 0, Integer.MAX_VALUE);
            System.out.println(targetIds.size());
            for(Integer id : targetIds){
                String feedsKey = RedisKeyUtil.getFeedsKey(id);
                redisTemplate.opsForList().leftPush(feedsKey, event.getEntityId());
            }
        }

    }

    @KafkaListener(topics = {TOPIC_ACTION})
    public void handleActionMessage(ConsumerRecord record){
        if(record == null || record.value() == null){
            logger.error("消息的内容为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event == null){
            logger.error("消息格式错误");
            return;
        }
        // 记录行为日志
        ActionLog actionLog = new ActionLog();
        actionLog.setUserId(event.getUserId());
        actionLog.setNewsId(event.getEntityId());
        actionLog.setActionType((int)event.getData().get("actionType"));
        actionLog.setActionTime(new Date());
        actionLogService.addActionLog(actionLog);

        // 加入近期操作过新闻集合
        String redisKey = RedisKeyUtil.getActionNewsKey();
        redisTemplate.opsForSet().add(redisKey, event.getEntityId());

        // 添加浏览记录
        if((int)event.getData().get("actionType") == ACTION_TYPE_VIEW){
            redisKey = RedisKeyUtil.getHistoryKey(event.getUserId());
            redisTemplate.opsForSet().add(redisKey, event.getEntityId());
            // 从推荐列表中删除该新闻
            redisKey = RedisKeyUtil.getRecommendKey(event.getUserId());
            redisTemplate.opsForZSet().remove(redisKey, event.getEntityId());
        }else{
            // 对正反馈行为推送新闻给相似用户
            // 获取行为用户的相似用户列表
            redisKey = RedisKeyUtil.getUserSimKey(event.getUserId());
            Set<Integer> ids = redisTemplate.opsForZSet().range(redisKey, 0, 9);
            for(Integer id : ids){
                // 是否已经读过
                String historyKey = RedisKeyUtil.getHistoryKey(id);
                if(!redisTemplate.opsForSet().isMember(historyKey, event.getEntityId())){
                    // 加入用户的推荐列表
                    redisKey = RedisKeyUtil.getRecommendKey(id);
                    redisTemplate.opsForZSet().add(redisKey, event.getEntityId(), new Date().getTime());
                    if(redisTemplate.opsForZSet().size(redisKey) > 100){
                        // 删除多余部分， 只保留100条推荐
                        redisTemplate.opsForZSet().removeRange(redisKey, 0, 0);
                    }
                }
            }
        }



    }




}
