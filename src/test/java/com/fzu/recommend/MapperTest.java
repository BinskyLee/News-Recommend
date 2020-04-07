package com.fzu.recommend;

import com.fzu.recommend.dao.*;
import com.fzu.recommend.entity.*;
import com.fzu.recommend.util.RecommendConstant;
import com.fzu.recommend.util.RecommendUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = RecommendApplication.class)
public class MapperTest implements RecommendConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NewsMapper newsMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private MessageMapper messageMapper;

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

    @Test
    public void testInsertComment(){
        Comment comment = new Comment();
        comment.setUserId(33);
        comment.setCreateTime(new Date());
        comment.setEntityType(ENTITY_TYPE_COMMENT);
        comment.setEntityId(1);
        comment.setContent("测试回复");
        commentMapper.insertComment(comment);
    }

    @Test
    public void testSelectCommentsByEntity(){
        List<Comment> comments = commentMapper.selectCommentsByEntity(2, 1, 0, 5);
        System.out.println(comments);
    }

    @Test
    public void testSelectCountByEntity(){
        int count = commentMapper.selectCountByEntity(2, 1);
        System.out.println(count);
    }

    @Test
    public void testSelectNewsById(){
        News news = newsMapper.selectNewsById(15);
        System.out.println(news);
    }

    @Test
    public void testInsertMessage(){
        Message message = new Message();
        for(int i = 1; i <= 10; i++) {
            message.setFromId(22);
            message.setToId(33);
            message.setContent("测试私信" + i);
            message.setConversationId("22_33");
            message.setCreateTime(new Date());
            message.setStatus(0);
            messageMapper.insertMessage(message);
        }

    }
    @Test
    public void testSelectMessage(){
        System.out.println(messageMapper.selectConversationCount(33));
        System.out.println(messageMapper.selectLetterCount("22_33"));
        System.out.println(messageMapper.selectConversations(33, 0, 5));
        System.out.println(messageMapper.selectLetters("22_33", 0, 5));
        System.out.println(messageMapper.selectLetterUnreadCount(33, null ));
        List<Integer> ids = new ArrayList<>();
        ids.add(1);
        ids.add(2);
        messageMapper.updateStatus(ids, 1);

    }

}
