package com.fzu.recommend.controller;

import com.fzu.recommend.annotation.LoginRequired;
import com.fzu.recommend.entity.Comment;
import com.fzu.recommend.entity.Event;
import com.fzu.recommend.entity.News;
import com.fzu.recommend.entity.Page;
import com.fzu.recommend.event.EventProducer;
import com.fzu.recommend.service.CommentService;
import com.fzu.recommend.service.NewsService;
import com.fzu.recommend.util.HostHolder;
import com.fzu.recommend.util.RecommendConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

@Controller
public class CommentController implements RecommendConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private NewsService newsServicel;

    @LoginRequired
    @RequestMapping(path = "/comment/add/{newsId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("newsId") int newsId, Comment comment){
        if(comment == null || StringUtils.isBlank(comment.getContent())){
            return "redirect:/news/detail/" + newsId;
        }

        comment.setUserId(hostHolder.getUser().getId());
        comment.setCreateTime(new Date());
        commentService.addComment(comment, newsId);
        //触发评论事件
        int targetId = 0;
        if(comment.getEntityType() == ENTITY_TYPE_NEWS){
            targetId = newsServicel.findNewsById(comment.getEntityId()).getUserId();
        }else if(comment.getEntityType() == ENTITY_TYPE_COMMENT){
            targetId = commentService.findCommentById(comment.getEntityId()).getUserId();
        }
        if(hostHolder.getUser().getId() != targetId) { //自己评论自己不通知
            Event event = new Event()
                    .setTopic(TOPIC_COMMENT)
                    .setEntityType(comment.getEntityType())
                    .setEntityId(comment.getEntityId())
                    .setUserId(hostHolder.getUser().getId())
                    .setData("newsId", newsId)
                    .setEntityUserId(targetId);
            eventProducer.fireEvent(event);
        }
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(comment.getUserId())
                .setEntityType(ENTITY_TYPE_NEWS)
                .setEntityId(newsId);
        eventProducer.fireEvent(event);

        return "redirect:/news/detail/" + newsId;
    }







}
