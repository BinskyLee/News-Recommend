package com.fzu.recommend.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecommendUtil {

    private static final Logger logger = LoggerFactory.getLogger(RecommendUtil.class);

    // 生成随机字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    //MD5加密
    public static String md5(String key){
        if(StringUtils.isBlank(key))
            return null;
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if (map != null) {
            for (String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }

    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }

    public static Map<String, Object> uploadImg(MultipartFile file, String uploadPath) throws IOException{
        Map<String, Object> map = new HashMap<>();
        String fileName = file.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            map.put("msg", "上传图片不能为空");
            return map;
        }
        if(".gif".equals(suffix) || ".jpg".equals(suffix) || ".jpeg".equals(suffix) || ".png".equals(suffix)){
            fileName = RecommendUtil.generateUUID() + suffix;
            File dest = new File(uploadPath + "/" + fileName);
            file.transferTo(dest);
            map.put("fileName", fileName);
            return map;
        }
        map.put("msg", "图片格式错误");
        return map;
    }

    public static void getImg(String fileName, String uploadPath, OutputStream os) {
        //服务器存放路径
        fileName = uploadPath + "/" + fileName;
        //文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fileName);
            byte[] buffer = new byte[1024];
            int b = 0;
            while((b = fis.read(buffer)) != -1){
                os.write(buffer, 0, b);
            }
        }catch (IOException e) {
            logger.error("获取图像失败:" + e.getMessage());
        }finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                logger.error("关闭输入流失败:" + e.getMessage());
            }
            try {
                if(os != null) {
                    os.close();
                }
            } catch (Exception e) {
                logger.error("关闭输出流失败:" + e.getMessage());
            }
        }
    }

    public static String htmlReplace(String str){
        String reg="<[^>]+>"; //定义HTML标签的正则表达式
        Pattern pattern=Pattern.compile(reg,Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        str = matcher.replaceAll(""); //过滤html标签
        return str;
    }

    public static String imgReplace(String str){
        String reg="<figure[^>]*?>[\\s\\S]*?<\\/figure>";
        Pattern pattern=Pattern.compile(reg,Pattern.CASE_INSENSITIVE);
        Matcher matcher= pattern.matcher(str);
        str = matcher.replaceAll(""); //过滤html标签
        System.out.println(str);
        return str;
    }

    public static String stringFilter(String str){
        String regEx = "[`\\-~!@#$^&*()=|{}':;',\\[\\].<>/?~！@#￥……&*（）——{}【】‘<>；：丨”“'。〒，、？「」•+͈]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("。");
    }

    public static Set intersection(Set s1, Set s2){
        Set set = new HashSet();
        set.addAll(s1);
        set.retainAll(s2);
        return set;
    }

    public static Set union(Set s1, Set s2){
        Set set = new HashSet();
        set.addAll(s1);
        set.addAll(s2);
        return set;
    }


}
