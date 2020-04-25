package com.fzu.recommend.controller;

import com.fzu.recommend.entity.News;
import com.fzu.recommend.entity.Page;
import com.fzu.recommend.entity.User;
import com.fzu.recommend.service.*;
//import com.sun.org.apache.xpath.internal.operations.Mod;
import com.fzu.recommend.util.HostHolder;
import com.fzu.recommend.util.RecommendConstant;
import com.fzu.recommend.util.RecommendUtil;
import com.fzu.recommend.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Autowired
    private RecommendService recommendService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DataService dataService;


    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page){
        return "forward:/index"; //转发请求
    }

    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexCategoryPage(Model model, Page page, @RequestParam(value = "categoryId", required = false, defaultValue = "0") int categoryId ,
                                       @RequestParam(value = "sort", required = false, defaultValue = "0") int sort){
        page.setPath("/index?categoryId=" + categoryId + "&sort=" + sort);
        page.setLimit(10);
        List<News> list = new ArrayList<>();
        if(categoryId != 0){
            page.setRows(newsService.findNewsRows(0, categoryId));
            list = newsService.findNews(0, page.getOffset(), page.getLimit(), categoryId, sort);
        }else {
            // 冷启动，对于推荐数小于10或未登录用户，推荐最热门的100条新闻
            int count = hostHolder.getUser() == null ? 0 :
                    (int) recommendService.getRecommendRows(hostHolder.getUser().getId());
            if (count < 10) {
                // 推荐最热门的100条新闻
                page.setRows(100);
                list = newsService.findNews(0, page.getOffset(), page.getLimit(), 0, 2);

            } else {
                int userId = hostHolder.getUser().getId();
                page.setRows((int) recommendService.getRecommendRows(userId));
                list = recommendService.getRecommendNews(userId, page.getOffset(), page.getLimit());
            }
        }
        List<Map<String, Object>> news = new ArrayList<>();
        if(list != null){
            for(News item : list){
                // 文章简介去html
                item.setContent(RecommendUtil.htmlReplace(item.getContent()));
                // 浏览数
                item.setClickCount((int)dataService.getViewCount(item.getId(), item.getClickCount()));

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

        // 近期热闻
        List<News> hotNews= newsService.findNews(0, 0, 10, 0, 2);
        model.addAttribute("hotNews", hotNews);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("sortName", sortName[sort]);

        return "/index";
    }

    @RequestMapping(path = "/error", method = RequestMethod.GET)
    public String getErrorPage(){
        return "/error/500";
    }



}
