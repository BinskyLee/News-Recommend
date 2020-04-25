package com.fzu.recommend;

import com.fzu.recommend.entity.News;
import com.fzu.recommend.recommend.UserSimilarity;
import com.fzu.recommend.recommend.UserTag;
import com.fzu.recommend.recommend.UserToUser;
import com.fzu.recommend.service.NewsService;
import com.fzu.recommend.util.EncodeUtil;
import com.fzu.recommend.util.RecommendConstant;
import com.fzu.recommend.util.RecommendUtil;
import com.fzu.recommend.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = RecommendApplication.class)
public class RecommendTest implements RecommendConstant {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private NewsService newsService;

    @Autowired
    private UserSimilarity userSimilarity;

    @Autowired
    private UserTag userTag;


    @Test
    public void testTime(){
        Calendar rightNow = Calendar.getInstance();
        rightNow.setTime(new Date());
        rightNow.add(Calendar.DATE, 30);
        System.out.println(rightNow.getTime());
        //转化String 为日期
    }

    @Test
    public void testString(){
        String str = "<p>不知道能不能成功</p><p>不要乱插入图片</p>";
        int st = str.indexOf("<p>");
        System.out.println(st);
        int ed = str.indexOf("</p>");
        System.out.println(ed);
    }

    @Test
    public void testSensitive(){
        String str = "！#@#色@#！#！情海luo因足球￥#！投注试试";
        System.out.println(sensitiveFilter.filter(str));

    }

    @Test
    public void testKeywords(){
        List<News> list = newsService.findNews(0, 1, Integer.MAX_VALUE, 0 ,0);
        for(News news : list){
            news.setContent(RecommendUtil.imgReplace(news.getContent()));
            String keywords = newsService.getKeywords(news);
            newsService.updateKeywords(news.getId(), keywords);
        }
    }

    @Test
    public void testCharacter() throws UnsupportedEncodingException {
        String str = "雅虎";
        str = EncodeUtil.encode(str, "UTF-8");
        System.out.println(str);
        str = EncodeUtil.decode(str, "UTF-8");
        System.out.println(str);
    }

    @Test
    public void testUserSim(){
        userSimilarity.calSimilarity();;
    }

    @Test
    public void testMap(){
        userTag.calUserTag();
    }

}


