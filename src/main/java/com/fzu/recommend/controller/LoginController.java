package com.fzu.recommend.controller;

import com.fzu.recommend.entity.User;
import com.fzu.recommend.service.UserService;
import com.fzu.recommend.util.RecommendConstant;
import com.fzu.recommend.util.RecommendUtil;
import com.fzu.recommend.util.RedisKeyUtil;
import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements RecommendConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @RequestMapping(path="/register", method = RequestMethod.POST)
    public String register(Model model, User user){
        Map<String, Object> map = userService.register(user);
        if(map == null || map.isEmpty()){
            model.addAttribute("msg", "注册成功,我们已经向您的邮箱发送了一封验证邮件,请激活您的帐号。");
            model.addAttribute("target", "/");
            return "/site/operate-result";
        }else{
            System.out.println(map.get("msg"));
            model.addAttribute("msg", map.get("msg"));
            return "/site/register";
        }
    }

    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code){
        int result = userService.activation(userId, code);
        if(result == ACTIVATION_SUCCESS){
            model.addAttribute("msg", "您的账号已经成功激活。");
            model.addAttribute("target", contextPath + "/login");
        }else if(result == ACTIVATION_REPEAT){
            model.addAttribute("msg", "此邮箱已激活过了,不可重复激活。");
            model.addAttribute("target", contextPath + "/");
        }else if(result == ACTIVATION_FAILURE){
            model.addAttribute("msg", "此邮箱认证链接无效。");
            model.addAttribute("target", contextPath + "/");
        }
        return "/site/operate-result";
    }

    @RequestMapping(path="/login", method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }


    @ResponseBody
    @RequestMapping(path="/check/username", method = RequestMethod.POST)
    public boolean checkUsername(String username){
        if(userService.usernameExist(username))
            return false;
        return true;
    }

    @ResponseBody
    @RequestMapping(path="/check/email", method = RequestMethod.POST)
    public boolean checkEmail(String email){
        if(userService.emailExist(email)){
            return false;
        }
        return true;
    }

    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session){
        //生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage img = kaptchaProducer.createImage(text);
        //验证码的归属
        String kaptchaOwner = RecommendUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);;
        //将验证码存入Redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);

        //将图片输出给浏览器
        //设置输出格式
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(img, "png", os);
        } catch (IOException e) {
            logger.error("获取验证码失败:" + e.getMessage());
        }
    }


    @RequestMapping(path = "/login", method = RequestMethod.POST)
    @ResponseBody
    public String login(String email, String password, String code, boolean rememberme,
                        HttpServletResponse response,
                        @CookieValue( value = "kaptchaOwner", required = false) String kaptchaOwner){
        String kaptcha = null;
        if(StringUtils.isNotBlank(kaptchaOwner)){
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String)redisTemplate.opsForValue().get(redisKey);
        }else{
            return RecommendUtil.getJSONString(2, "验证码失效");
        }
        if(StringUtils.isBlank(kaptcha) || !kaptcha.equals(code)){
            return RecommendUtil.getJSONString(1, "验证码错误");
        }
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDES;
        Map<String, Object> map = userService.login(email, password, expiredSeconds);
        String ticket = (String)map.get("ticket");
        if(!StringUtils.isBlank(ticket)){
            Cookie cookie = new Cookie("ticket", ticket);
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return RecommendUtil.getJSONString(0, "登陆成功");
        }else{
            return RecommendUtil.getJSONString(3, (String)map.get("msg"));
        }
    }

    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        return "redirect:/";
    }


}
