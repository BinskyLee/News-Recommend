package com.fzu.recommend.util;

public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like";
    private static final String PREFIX_FAVORITE = "favorite";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_SIMILARITY = "similarity";


    //某个实体的赞
    //like:entity:entityType:entityId > set(userId) (存储实体的点赞集合)
    public static String getEntityLikeKey(int entityType, int entityId){
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    //某个新闻的收藏数
    //favorite:news:newsId > set(userId)
    public static String getNewsFavoriteKey(int newsId){
        return PREFIX_FAVORITE + SPLIT + "news" + SPLIT + newsId;
    }

    //某个用户的收藏新闻
    //favorite:user:userId > set(newsId)
    public static String getUserFavoriteKey(int userId){
        return PREFIX_FAVORITE + SPLIT + "user" + SPLIT + userId;
    }

    //某个用户的关注实体
    //followee:userId:entityType -> zSet(entityId, now)
    public static String getFolloweeKey(int userId, int entityType){
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    //某个用户的粉丝
    //follower:entityType:entityId ->zSet(userId, now)
    public static String getFollowerKey(int entityType, int entityId){
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    // 登陆验证码
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA + SPLIT + owner;

    }

    // 登陆凭证
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }

    // 用户信息
    public static String getUserKey(int userId){
        return PREFIX_TICKET + SPLIT + userId;
    }

    // 相似度
    public static String getSimilarityKey(int newsId){
        return PREFIX_SIMILARITY + SPLIT + newsId;
    }



}
