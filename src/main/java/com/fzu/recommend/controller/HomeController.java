package com.fzu.recommend.controller;

import com.fzu.recommend.entity.News;
import com.fzu.recommend.entity.Page;
import com.fzu.recommend.entity.User;
import com.fzu.recommend.service.LikeService;
import com.fzu.recommend.service.NewsService;
import com.fzu.recommend.service.UserService;
//import com.sun.org.apache.xpath.internal.operations.Mod;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class HomeController implements RecommendConstant {

    @Autowired
    private NewsService newsService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page){
        page.setRows(newsService.findNewsRows(0));
        page.setPath("/");
        page.setLimit(5);
        List<News> list = newsService.findNews(0, page.getOffset(), page.getLimit());
        List<Map<String, Object>> news = new ArrayList<>();
        if(list != null){
            for(News item : list){
                //文章简介去html
                item.setContent(RecommendUtil.htmlReplace(item.getContent()));

                Map<String, Object> map = new HashMap<>();
                map.put("news", item);
                User user = userService.findUserById(item.getUserId());
                map.put("user", user);

                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_NEWS, item.getId());
                map.put("likeCount", likeCount);

                news.add(map);
            }
        }
        model.addAttribute("news", news);
        return "/index";
    }

    @RequestMapping(path = "/error", method = RequestMethod.GET)
    public String getErrorPage(){
        return "/error/500";
    }


    @RequestMapping(path = "/followee", method = RequestMethod.GET)
    public ModelAndView getNewsPage(Model model, @RequestParam(value = "current", defaultValue = "1", required = false) int current, boolean async){
        //方法调用前，SpringMVC会自动实例化Page和Model，并将Page注入Model
        //所以在Thymeleaf中可以直接访问Page对象中的数据
        Page page = new Page();
        page.setRows(newsService.findNewsRows(0));
        page.setPath("/");
        page.setCurrent(current);
        model.addAttribute("page", page);
        List<News> list = newsService.findNews(0, page.getOffset(), page.getLimit());
        List<Map<String, Object>> news = new ArrayList<>();
        if(list != null){
            for(News item : list){
                //文章简介去html
                item.setContent(RecommendUtil.htmlReplace(item.getContent()));
                Map<String, Object> map = new HashMap<>();
                map.put("news", item);
                User user = userService.findUserById(item.getUserId());
                map.put("user", user);
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_NEWS, item.getId());
                map.put("likeCount", likeCount);
                news.add(map);
            }
        }
        model.addAttribute("news", news);
        return new ModelAndView(async == true?  "/site/followee::.news-list":"/site/followee");
    }

}
