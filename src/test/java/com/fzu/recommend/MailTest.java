package com.fzu.recommend;

import com.fzu.recommend.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = RecommendApplication.class)
public class MailTest {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testTextMail(){
        mailClient.sendMail("471393460@qq.com", "Test", "想玩动物之森");
    }

    @Test
    public void testHtmlMail(){
        Context context = new Context();
        context.setVariable("username", "Mary");

        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);

        mailClient.sendMail("471393460@qq.com", "Test", content);


    }

}
