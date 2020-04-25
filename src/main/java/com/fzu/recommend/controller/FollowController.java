package com.fzu.recommend.controller;


import com.fzu.recommend.annotation.LoginRequired;
import com.fzu.recommend.entity.Event;
import com.fzu.recommend.entity.News;
import com.fzu.recommend.entity.Page;
import com.fzu.recommend.entity.User;
import com.fzu.recommend.event.EventProducer;
import com.fzu.recommend.service.FollowService;
import com.fzu.recommend.service.LikeService;
import com.fzu.recommend.service.NewsService;
import com.fzu.recommend.service.UserService;
import com.fzu.recommend.util.HostHolder;
import com.fzu.recommend.util.RecommendConstant;
import com.fzu.recommend.util.RecommendUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements RecommendConstant {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private FollowService followService;

    @Autowired
    private UserService userService;

    @Autowired
    private NewsService newsService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private LikeService likeService;

    //关注
    @LoginRequired
    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId){
        User user = hostHolder.getUser();
        if(entityType == ENTITY_TYPE_USER && user.getId() == entityId){
            return RecommendUtil.getJSONString(1, "不能关注自己");
        }
        //关注
        followService.follow(user.getId(), entityType, entityId);
        //关注状态
        boolean hasFollowed = followService.hasFollowed(user.getId(), entityType, entityId);
        //粉丝数量
        long followerCount = followService.findFollowerCount(entityType, entityId);
        Map<String, Object> map = new HashMap<>();
        map.put("hasFollowed", hasFollowed);
        map.put("followerCount", followerCount);
        //触发事件
        if(hasFollowed) {
            Event event = new Event()
                    .setTopic(TOPIC_FOLLOW)
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityUserId(entityId);
            eventProducer.fireEvent(event);
        }
        return RecommendUtil.getJSONString(0, null, map);
    }

    @RequestMapping(path = "/user/profile/{userId}/followee", method = RequestMethod.GET)
    public ModelAndView getUserFollowee(@PathVariable("userId") int userId, Model model,
                                       @RequestParam(value = "current", required = false) int current){
        //用户信息
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在");
        }
        //关注用户
        Page followeePage = new Page();
        followeePage.setPath("/user/profile/" + userId + "/followee");
        followeePage.setRows((int)followService.findFolloweeCount(userId, ENTITY_TYPE_USER));
        followeePage.setLimit(5);
        followeePage.setCurrent(current);
        List<User> followeeList = followService.findFolloweeList(userId, followeePage.getOffset(), followeePage.getLimit());
        model.addAttribute("followeeList", getUserVOList(followeeList));
        model.addAttribute("followeePage", followeePage);
        return new ModelAndView("/site/profile::#followee-content-view");
    }

    @RequestMapping(path = "/user/profile/{userId}/follower", method = RequestMethod.GET)
    public ModelAndView getUserFollower(@PathVariable("userId") int userId, Model model,
                                        @RequestParam(value = "current", required = false) int current){
        //用户信息
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在");
        }
        //粉丝列表
        Page followerPage = new Page();
        followerPage.setPath("/user/profile/" + userId + "/follower");
        followerPage.setRows((int)followService.findFollowerCount(ENTITY_TYPE_USER, userId));
        followerPage.setLimit(5);
        followerPage.setCurrent(current);
        List<User> followerList = followService.findFollowerList(userId, followerPage.getOffset(), followerPage.getLimit());
        model.addAttribute("followerList", getUserVOList(followerList));
        model.addAttribute("followerPage", followerPage);
        return new ModelAndView("/site/profile::#follower-content-view");
    }

    private List<Map<String,Object>> getUserVOList(List<User> users){
        List<Map<String,Object>> list = new ArrayList<>();
        for(User user : users){
            Map<String, Object> map = new HashMap<>();
            map.put("user", user);
            map.put("followeeCount", followService.findFolloweeCount(user.getId(), ENTITY_TYPE_USER));
            map.put("followerCount", followService.findFollowerCount(user.getId(), ENTITY_TYPE_USER));
            map.put("hasFollowed", hostHolder.getUser() == null ? false :
                    followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, user.getId()));
            map.put("newsCount", newsService.findNewsRows(user.getId(), 0));
            list.add(map);
        }
        return list;
    }


}
