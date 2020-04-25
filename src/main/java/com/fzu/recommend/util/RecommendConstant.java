package com.fzu.recommend.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public interface RecommendConstant {


    /**激活成功
     *
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = 2;

    /**
     * 默认失效时间12小时
     */
    int DEFAULT_EXPIRED_SECONDES = 12 * 3600;

    /**
     * 记住我时失效时间30天
     */
    int REMEMBER_EXPIRED_SECONDS = 30 * 24 * 3600;

    /**
     * 实体类型：新闻
     */
    int ENTITY_TYPE_NEWS = 1;

    /**
     * 实体类型：评论
     */
    int ENTITY_TYPE_COMMENT = 2;

    /**
     * 实体类型：用户
     */
    int ENTITY_TYPE_USER = 3;

    /**
     * 主题：评论
     */
    String TOPIC_COMMENT = "comment";

    /**
     * 主题：发布
     */
    String TOPIC_PUBLISH = "publish";


    /**
     * 主题：点赞
     */
    String TOPIC_LIKE = "like";

    /**
     * 主题：关注
     */
    String TOPIC_FOLLOW = "follow";

    /**
     * 主题：动作
     */
    String TOPIC_ACTION = "action";


    /**
     * 系统用户ID
     */
    int SYSTEM_USER_ID = 1;

    /**
     * 行为类型：浏览
     */
    int ACTION_TYPE_VIEW = 1;

    /**
     * 行为类型：点赞
     */
    int ACTION_TYPE_LIKE = 2;

    /**
     * 行为类型：评论
     */
    int ACTION_TYPE_COMMENT = 3;

    /**
     * 行为类型：收藏
     */
    int ACTION_TYPE_FAVORITE = 4;

    /**
     * 排序名
     */
    String[] sortName = {"投稿时间排序", "评论数排序", "热度排序", "收藏数排序", "点赞数排序"};

    /**
     * 分类名
     */
    String[] categoryName = {"推荐", "科技", "生活", "动画", "游戏", "娱乐", "体育", "财经", "国际", "军事"};








}
