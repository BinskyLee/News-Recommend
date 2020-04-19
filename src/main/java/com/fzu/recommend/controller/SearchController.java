package com.fzu.recommend.controller;

import com.fzu.recommend.entity.News;
import com.fzu.recommend.entity.Page;
import com.fzu.recommend.service.ElasticsearchService;
import com.fzu.recommend.service.LikeService;
import com.fzu.recommend.service.UserService;
import com.fzu.recommend.util.RecommendConstant;
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
public class SearchController implements RecommendConstant {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model){
        org.springframework.data.domain.Page<News> searchResult =
                elasticsearchService.searchNews(keyword, page.getCurrent() - 1, page.getLimit());
        //聚合数据
        List<Map<String, Object>> newsVOList = new ArrayList<>();
        if(searchResult != null){
            for(News news : searchResult){
                Map<String, Object> newsVO = new HashMap<>();
                newsVO.put("news", news);
                newsVO.put("user", userService.findUserById(news.getUserId()));
                newsVO.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_NEWS, news.getId()));

                newsVOList.add(newsVO);
            }
        }
        model.addAttribute("newsVOList", newsVOList);
        model.addAttribute("keyword", keyword);
        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchResult == null ? 0 : (int)searchResult.getTotalElements());

        return "/site/search";

    }
}
