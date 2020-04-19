package com.fzu.recommend;

import com.fzu.recommend.entity.News;
import com.fzu.recommend.service.NewsService;
import com.fzu.recommend.util.RecommendConstant;
import com.fzu.recommend.util.RecommendUtil;
import com.fzu.recommend.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
        List<News> list = newsService.findNews(0, 1, Integer.MAX_VALUE);
        for(News news : list){
            news.setContent(RecommendUtil.imgReplace(news.getContent()));
            newsService.updateKeywords(news);
        }
    }
}
