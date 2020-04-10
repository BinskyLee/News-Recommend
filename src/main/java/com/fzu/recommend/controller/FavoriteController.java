package com.fzu.recommend.controller;

import com.fzu.recommend.annotation.LoginRequired;
import com.fzu.recommend.entity.Page;
import com.fzu.recommend.entity.User;
import com.fzu.recommend.service.FavoriteService;
import com.fzu.recommend.service.LikeService;
import com.fzu.recommend.util.HostHolder;
import com.fzu.recommend.util.RecommendUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FavoriteController {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private FavoriteService favoriteService;


    @LoginRequired
    @RequestMapping(path = "/favorite", method = RequestMethod.POST)
    @ResponseBody
    public String favorite(int newsId){
        User user = hostHolder.getUser();
        //收藏
        favoriteService.favorite(user.getId(), newsId);
        //收藏数
        long favoriteCount = favoriteService.findNewsFavoriteCount(newsId);
        //收藏状态
        boolean favoriteStatus = favoriteService.findUserFavoriteStatus(user.getId(), newsId);
        Map<String, Object> map = new HashMap<>();
        map.put("favoriteCount", favoriteCount);
        map.put("favoriteStatus", favoriteStatus);
        return RecommendUtil.getJSONString(0, null, map);
    }

    @LoginRequired
    @RequestMapping(path = "/favorites", method = RequestMethod.GET)
    public String getFavoriteNews(Page page, Model model){
        User user = hostHolder.getUser();
        //分页信息
        page.setLimit(5);
        page.setPath("/favorites");
        page.setRows((int) favoriteService.findUserFavoriteCount(user.getId()));

        List<Map<String, Object>> newsList = favoriteService.findFavoriteNews(user.getId(), page.getOffset(), page.getLimit());
        model.addAttribute("newsList", newsList);
        model.addAttribute("favCount",favoriteService.findUserFavoriteCount(user.getId()));
        return "/site/favorites";
    }

}
