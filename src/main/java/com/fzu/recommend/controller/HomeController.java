package com.fzu.recommend.controller;

import com.fzu.recommend.entity.News;
import com.fzu.recommend.entity.Page;
import com.fzu.recommend.entity.User;
import com.fzu.recommend.service.NewsService;
import com.fzu.recommend.service.UserService;
//import com.sun.org.apache.xpath.internal.operations.Mod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private NewsService newsService;

    @Autowired
    private UserService userService;

//    @RequestMapping("/")
//    public String getIndexPage(){
//        return "/index";
//    }
    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page){
        //方法调用前，SpringMVC会自动实例化Page和Model，并将Page注入Model
        //所以在Thymeleaf中可以直接访问Page对象中的数据
        page.setRows(newsService.findNewsRows(0));
        page.setPath("/");
        List<News> list = newsService.findNews(0, page.getOffset(), page.getLimit());
        List<Map<String, Object>> news = new ArrayList<>();
        if(list != null){
            for(News item : list){
                Map<String, Object> map = new HashMap<>();
                map.put("news", item);
                User user = userService.findUserById(item.getUserId());
                map.put("user", user);
                news.add(map);
            }
        }
        model.addAttribute("news", news);
        return "/index";
    }
}
