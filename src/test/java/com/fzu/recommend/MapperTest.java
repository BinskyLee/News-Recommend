package com.fzu.recommend;

import com.fzu.recommend.dao.LoginTicketMapper;
import com.fzu.recommend.dao.NewsMapper;
import com.fzu.recommend.dao.UserMapper;
import com.fzu.recommend.entity.LoginTicket;
import com.fzu.recommend.entity.News;
import com.fzu.recommend.entity.User;
import com.fzu.recommend.util.RecommendUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = RecommendApplication.class)
public class MapperTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NewsMapper newsMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(1);
        System.out.println(user);
        user = userMapper.selectByName("test");
        System.out.println(user);
        user = userMapper.selectByEmail("test@qq.com");
        System.out.println(user);
    }

    @Test
    public void testInsertUser(){
        User user = new User();
        user.setUsername("test");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());
        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void testUpdateUser(){
        int rows = userMapper.updateStatus(1, 1);
        System.out.println(rows);
        rows = userMapper.updateHeader(1, "http://nowcoder.com/1.png");
        System.out.println(rows);
        rows = userMapper.updatePassword(1, "654321");
        System.out.println(rows);
    }

    @Test
    public void testSelectNews(){
        List<News> list = newsMapper.selectNews(1, 0, 10);
        for(News news : list){
            System.out.println(news);
        }

        int rows = newsMapper.selectNewsRows(1);
        System.out.println(rows);
    }

    @Test
    public void testInsertLoginTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(1);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 3600 * 30 * 1000));
        loginTicket.setTicket(RecommendUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectByTicket(){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("b47610ed403a4b02a355e01ff1c73b79");
        System.out.println(loginTicket);
    }

    @Test
    public void testUpdateStatus(){
        loginTicketMapper.updateStatus("b47610ed403a4b02a355e01ff1c73b79", 1);
    }


}
