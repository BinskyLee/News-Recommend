package com.fzu.recommend.service;

import com.fzu.recommend.dao.LoginTicketMapper;
import com.fzu.recommend.dao.UserMapper;
import com.fzu.recommend.entity.LoginTicket;
import com.fzu.recommend.entity.User;
import com.fzu.recommend.util.MailClient;
import com.fzu.recommend.util.RecommendUtil;
import com.fzu.recommend.util.RecommendConstant;
import com.fzu.recommend.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class UserService implements RecommendConstant{

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${recommend.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id) {
        User user = getCache(id);
        if(user == null){
            user = initCache(id);
        }
        return user;
//        return userMapper.selectById(id);
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

    public Map<String, Object> register(User user){
        Map<String, Object> map = new HashMap<>();
        //空值处理
        if(user == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        if(StringUtils.isBlank(user.getEmail()) || StringUtils.isBlank(user.getUsername())
            || StringUtils.isBlank(user.getPassword())){
            map.put("msg", "参数不能为空");
            return map;
        }
        //正则处理
        String regEmail = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        String regName = "^[\\u4e00-\\u9fa5_a-zA-Z0-9]{2,16}$";
        String regPwd = "^\\w{6,20}$";
        Matcher emailMatcher = Pattern.compile(regEmail).matcher(user.getEmail());
        Matcher nameMatcher = Pattern.compile(regName).matcher(user.getUsername());
        Matcher pwdMatcher = Pattern.compile(regPwd).matcher(user.getPassword());
        if(!emailMatcher.matches() || !nameMatcher.matches() || !pwdMatcher.matches()){
            map.put("msg", "参数错误");
            return map;
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
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "验证电子邮箱", content);
        return map;
    }

    public int activation(int userId, String code){
        User user = userMapper.selectById(userId);
        if(user == null){ //非法用户ID
            return ACTIVATION_FAILURE;
        }else if(user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId, 1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        }else{
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String, Object> login(String email, String password, int expiredSeconds){
        Map<String, Object> map = new HashMap<>();
        //空值处理
        if(StringUtils.isBlank(email) || StringUtils.isBlank(password)){
            map.put("msg", "参数不能为空");
            return map;
        }
        User user = userMapper.selectByEmail(email);
        if(user == null){
            map.put("msg", "用户不存在");
            return map;
        }
        if(user.getStatus() == 0){
            map.put("msg", "帐号未激活");
            return map;
        }
        password = RecommendUtil.md5(password + user.getSalt());
        if(!user.getPassword().equals(password)){
            map.put("msg", "密码错误");
            return map;
        }
        //生成登陆凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(RecommendUtil.generateUUID());
//        loginTicket.setStatus(0);
//        Calendar now = Calendar.getInstance();
//        now.setTime(new Date());
//        now.add(Calendar.SECOND, expiredSeconds);
//        loginTicket.setExpired(now.getTime());
//        loginTicketMapper.insertLoginTicket(loginTicket);
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket, expiredSeconds, TimeUnit.SECONDS);
        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public LoginTicket findLoginTicket(String ticket){
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    public void logout(String ticket) {
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        redisTemplate.delete(redisKey);
//        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);

//        loginTicket.setStatus(1);
//        redisTemplate.opsForValue().set(redisKey, loginTicket);

    }

    public int updateHeader(int userId, String headerUrl) {
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;

    }

    public boolean updatePassword(User user, String oldPassword, String newPassword){
        //空值判断
        if(user == null || StringUtils.isBlank(oldPassword) || StringUtils.isBlank(newPassword)){
            return false;
        }
        if(RecommendUtil.md5(oldPassword + user.getSalt()).equals(user.getPassword())){
            userMapper.updatePassword(user.getId(),RecommendUtil.md5(newPassword + user.getSalt()));
            clearCache(user.getId());
            return true;
        }else{
            return false;
        }
    }

    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }

    //优先从缓存中取数据
    private User getCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User)redisTemplate.opsForValue().get(redisKey);
    }
    //取不到初始化缓存
    private User initCache(int userId){
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }
    //数据变更时清楚缓存
    private void clearCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }
}
