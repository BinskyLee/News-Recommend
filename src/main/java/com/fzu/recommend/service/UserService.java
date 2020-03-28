package com.fzu.recommend.service;

import com.fzu.recommend.dao.UserMapper;
import com.fzu.recommend.entity.User;
import com.fzu.recommend.util.MailClient;
import com.fzu.recommend.util.RecommendUtil;
import com.fzu.recommend.util.RecommendConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.Random;



@Service
public class UserService implements RecommendConstant{

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${recommend.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id){
        return userMapper.selectById(id);
    }

    public boolean usernameExist(String username){
        if(userMapper.selectByName(username) != null)
            return true;
        return false;
    }

    public boolean emailExist(String email){
        if(userMapper.selectByEmail(email) != null)
            return true;
        return false;
    }

    public void register(User user){
        //空值处理
        if(user == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        //添加用户
        user.setSalt(RecommendUtil.generateUUID().substring(0, 5));
        user.setPassword(RecommendUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(RecommendUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);
        //激活邮件
        Context context = new Context();
        context.setVariable("username", user.getUsername());
        String url = domain + contextPath + "activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "验证电子邮箱", content);
    }

    public int activation(int userId, String code){
        User user = userMapper.selectById(userId);
        if(user == null){ //非法用户ID
            return ACTIVATION_FAILURE;
        }else if(user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        }else{
            return ACTIVATION_FAILURE;
        }
    }

}
