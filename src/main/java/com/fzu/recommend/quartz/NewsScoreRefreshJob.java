package com.fzu.recommend.quartz;


import com.fzu.recommend.service.NewsService;
import com.fzu.recommend.util.RecommendConstant;
import com.fzu.recommend.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;


public class NewsScoreRefreshJob implements Job , RecommendConstant {

    private static final Logger logger = LoggerFactory.getLogger(NewsScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private NewsService newsService;


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        String redisKey = RedisKeyUtil.getActionNewsKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);
        if(operations.size() == 0){
            logger.info("任务取消，没有需要刷新分数的新闻");
            return;
        }
        logger.info("任务开始，正在计算新闻分数：" + operations.size());
        while(operations.size() > 0){
            newsService.refreshScore((int)operations.pop());
        }
        logger.info("任务结束，新闻分数刷新完毕");
    }


}
