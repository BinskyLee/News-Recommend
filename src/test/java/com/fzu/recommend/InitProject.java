package com.fzu.recommend;

import com.fzu.recommend.dao.UserMapper;
import com.fzu.recommend.dao.elasticsearch.NewsRepository;
import com.fzu.recommend.entity.News;
import com.fzu.recommend.entity.User;
import com.fzu.recommend.service.NewsService;
import com.fzu.recommend.service.RecommendService;
import com.fzu.recommend.service.UserService;
import com.fzu.recommend.util.RecommendUtil;
import org.junit.Test;
import org.junit.platform.commons.util.StringUtils;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Array;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = RecommendApplication.class)
public class InitProject {

    @Autowired
    private NewsService newsService;

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private RecommendService recommendService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    // 初始化关键词
    @Test
    public void initKeywords(){
        List<News> list = newsService.findNews(0, 0, Integer.MAX_VALUE, 0, 0);
        for(News news : list){
            String keywords = newsService.getKeywords(news);
            newsService.updateKeywords(news.getId(), keywords);
        }
    }

    // 初始化ES数据
    @Test
    public void initEsInsert(){
        List<News> list = newsService.findNews(0, 0, Integer.MAX_VALUE, 0, 0);
        for(News news : list){
            news.setContent(RecommendUtil.htmlReplace(news.getContent()));
        }
        newsRepository.saveAll(list);
    }

    // 计算新闻相似度
    @Test
    public void initNewsSimilarity(){
        List<News> list = newsService.findNews(0, 0, Integer.MAX_VALUE, 0, 0);
        for(int i = 0; i < list.size(); i++){
            News news1 = list.get(i);
            for(int j = i + 1; j < list.size(); j++){
                News news2 = list.get(j);
                recommendService.computeNewsSimilarity(news1, news2);
            }
        }
    }

    @Test
    public void testSimilarity(){
        List<News> list = recommendService.contentBasedRecommend(58);
        for(News news : list){
            System.out.println(news);
        }
    }

    // 创建用户
    @Test
    public void initUser(){
        // 生成250个用户
        for(int i = 0; i < 250; i++){
            User user = new User();
            user.setEmail("user" + i + "@qq.com");
            user.setUsername("用户" + RecommendUtil.generateUUID().substring(0, 5));
            user.setSalt(RecommendUtil.generateUUID().substring(0, 5));
            user.setPassword(RecommendUtil.md5("111111" + user.getSalt()));
            user.setType(0);
            user.setStatus(1);
            user.setActivationCode(RecommendUtil.generateUUID());
            user.setHeaderUrl(String.format("http://localhost:8080/recommend/user/header/%d.jpg", new Random().nextInt(11)));
            user.setCreateTime(new Date());
            userMapper.insertUser(user);
        }
    }

    // 随机生成数据



}
