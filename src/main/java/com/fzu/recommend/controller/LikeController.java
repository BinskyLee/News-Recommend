package com.fzu.recommend.controller;

import com.fzu.recommend.entity.Event;
import com.fzu.recommend.entity.User;
import com.fzu.recommend.event.EventProducer;
import com.fzu.recommend.service.LikeService;
import com.fzu.recommend.util.HostHolder;
import com.fzu.recommend.util.RecommendConstant;
import com.fzu.recommend.util.RecommendUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements RecommendConstant {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int newsId){
        User user = hostHolder.getUser();
        // 点赞
        likeService.like(user.getId(), entityType, entityId);
        // 数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        //状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        //返回的结果
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        //触发事件
        if(likeStatus == 1 && hostHolder.getUser().getId() != entityUserId){ //不给自己发事件
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityUserId(entityUserId)
                    .setData("newsId", newsId);
            eventProducer.fireEvent(event);
        }

        return RecommendUtil.getJSONString(0, null, map);
    }

}
