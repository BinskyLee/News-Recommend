package com.fzu.recommend.controller;


import com.alibaba.fastjson.JSONObject;
import com.fzu.recommend.annotation.LoginRequired;
import com.fzu.recommend.entity.News;
import com.fzu.recommend.entity.Page;
import com.fzu.recommend.entity.User;
import com.fzu.recommend.service.FollowService;
import com.fzu.recommend.service.LikeService;
import com.fzu.recommend.service.NewsService;
import com.fzu.recommend.service.UserService;
import com.fzu.recommend.util.HostHolder;
import com.fzu.recommend.util.RecommendConstant;
import com.fzu.recommend.util.RecommendUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements RecommendConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${recommend.path.upload}")
    private String uploadPath;

    @Value("${recommend.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private FollowService followService;

    @Autowired
    private NewsService newsService;

    @Autowired
    private LikeService likeService;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }


    @LoginRequired
    @RequestMapping(path = "/reset", method = RequestMethod.POST)
    @ResponseBody
    public boolean resetPassword(String oldPassword, String newPassword){
        User user = hostHolder.getUser();
        if(userService.updatePassword(user, oldPassword, newPassword)){
            return true;
        }else{
            return false;
        }
    }

    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImg, Model model) {
        try {
            Map<String, Object> map = RecommendUtil.uploadImg(headerImg, uploadPath);
            if(map.get("fileName") != null){
                String fileName = (String) map.get("fileName");
                User user = hostHolder.getUser();
                String headerUrl = domain + contextPath + "/user/header/" + fileName;
                userService.updateHeader(user.getId(), headerUrl);
                return "redirect:/";
            } else {
                model.addAttribute("imgMsg", map.get("msg"));
                return "/site/setting";
            }
        } catch (IOException e) {
            logger.error("上传文件失败:" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常", e);
        }
    }


    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        try{
            OutputStream os = response.getOutputStream();
            RecommendUtil.getImg(fileName, uploadPath, os);
        }catch (IOException e) {
            logger.error("获取头像失败:" + e.getMessage());
        }
    }

    //个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model){
        //用户信息
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在");
        }
        //用户
        model.addAttribute("user", user);
        model.addAttribute("followeeCount", followService.findFolloweeCount(userId, ENTITY_TYPE_USER));
        model.addAttribute("followerCount", followService.findFollowerCount(ENTITY_TYPE_USER, userId));
        model.addAttribute("newsCount", newsService.findNewsRows(userId));
        boolean hasFollowed = hostHolder.getUser() == null ? false :
                followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        model.addAttribute("hasFollowed", hasFollowed);
        //投稿新闻
        Page newsPage = new Page();
        newsPage.setPath("/user/profile/" + userId + "/article");
        newsPage.setRows(newsService.findNewsRows(userId));
        newsPage.setLimit(5);
        List<News> newsList = newsService.findNews(userId, newsPage.getOffset(), newsPage.getLimit());
        List<Map<String, Object>> newsVOList = new ArrayList<>();
        for(News news : newsList){
            Map<String, Object> map = new HashMap<>();
            news.setContent(RecommendUtil.htmlReplace(news.getContent()));
            map.put("news", news);
            map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_NEWS, news.getId()));
            newsVOList.add(map);
        }
        model.addAttribute("newsList", newsVOList);
        model.addAttribute("newsPage",newsPage);
        //关注用户
        Page followeePage = new Page();
        followeePage.setPath("/user/profile/" + userId + "/followee");
        followeePage.setRows((int)followService.findFolloweeCount(userId, ENTITY_TYPE_USER));
        followeePage.setLimit(5);
        List<User> followeeList = followService.findFolloweeList(userId, followeePage.getOffset(), followeePage.getLimit());
        model.addAttribute("followeeList", getUserVOList(followeeList));
        model.addAttribute("followeePage", followeePage);
        //粉丝列表
        Page followerPage = new Page();
        followerPage.setPath("/user/profile/" + userId + "/follower");
        followerPage.setRows((int)followService.findFollowerCount(ENTITY_TYPE_USER, userId));
        followerPage.setLimit(5);
        List<User> followerList = followService.findFollowerList(userId, followerPage.getOffset(), followerPage.getLimit());
        model.addAttribute("followerList", getUserVOList(followerList));
        model.addAttribute("followerPage", followerPage);
        return "/site/profile";
    }


    @RequestMapping(path = "/profile/{userId}/article", method = RequestMethod.GET)
    public ModelAndView getUserArticle(@PathVariable("userId") int userId, Model model,
                                       @RequestParam(value = "current", required = false) int current){
        //用户信息
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在");
        }
        Page newsPage = new Page();
        newsPage.setPath("/user/profile/" + userId + "/article");
        newsPage.setLimit(5);
        newsPage.setCurrent(current);
        newsPage.setRows(newsService.findNewsRows(userId));
        List<News> newsList = newsService.findNews(userId, newsPage.getOffset(), newsPage.getLimit());
        List<Map<String, Object>> newsVOList = new ArrayList<>();
        for(News news : newsList){
            Map<String, Object> map = new HashMap<>();
            news.setContent(RecommendUtil.htmlReplace(news.getContent()));
            map.put("news", news);
            map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_NEWS, news.getId()));
            newsVOList.add(map);
        }
        model.addAttribute("newsList", newsVOList);
        model.addAttribute("newsPage",newsPage);
        return new ModelAndView("/site/profile::#article-content-view");
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
            map.put("newsCount", newsService.findNewsRows(user.getId()));
            list.add(map);
        }
        return list;
    }
}
