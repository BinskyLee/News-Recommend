package com.fzu.recommend;

import com.fzu.recommend.dao.elasticsearch.NewsRepository;
import com.fzu.recommend.entity.News;
import com.fzu.recommend.service.NewsService;
import com.fzu.recommend.service.RecommendService;
import com.fzu.recommend.util.RecommendUtil;
import org.junit.Test;
import org.junit.platform.commons.util.StringUtils;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = RecommendApplication.class)
public class ProjectPrepare {

    @Autowired
    private NewsService newsService;

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private RecommendService recommendService;

    // 初始化关键词
    @Test
    public void initKeywords(){
        List<News> list = newsService.findNews(0, 0, Integer.MAX_VALUE);
        for(News news : list){
            newsService.updateKeywords(news);
        }
    }

    // 初始化ES数据
    @Test
    public void initEsInsert(){
        List<News> list = newsService.findNews(0, 0, Integer.MAX_VALUE);
        for(News news : list){
            news.setContent(RecommendUtil.htmlReplace(news.getContent()));
        }
        newsRepository.saveAll(list);
    }

    // 计算新闻相似度
    @Test
    public void initNewsSimilarity(){
        List<News> list = newsService.findNews(0, 0, Integer.MAX_VALUE);
        for(int i = 0; i < list.size(); i++){
            News news1 = list.get(i);
            for(int j = i + 1; j < list.size(); j++){
                News news2 = list.get(j);
            }
        }
        for(News news1 : list){
            for(News news2 : list){
                if(news1 == news2) {
                    continue;
                }
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


}
