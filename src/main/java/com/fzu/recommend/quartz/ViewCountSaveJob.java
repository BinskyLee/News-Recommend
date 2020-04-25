package com.fzu.recommend.quartz;

import com.fzu.recommend.entity.News;
import com.fzu.recommend.service.NewsService;
import com.fzu.recommend.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;

public class ViewCountSaveJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(ViewCountSaveJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private NewsService newsService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String prefix = RedisKeyUtil.getViewPrefix() + "*";
        Set<String> keys = redisTemplate.keys(prefix);
        if(keys.size() == 0){
            logger.info("任务取消，浏览数为空");
            return;
        }
        logger.info("任务开始，正在将数据持久化存储：" + keys.size());
        for(String key : keys){
            int newsId = Integer.parseInt(key.split(":")[1]);
            News news = newsService.findNewsById(newsId);
            newsService.updateClickCount(newsId, (int) (news.getClickCount() + redisTemplate.opsForHyperLogLog().size(key)));

            redisTemplate.opsForHyperLogLog().delete(key); //清空key
        }
        logger.info("任务结束，数据持久化完毕");


    }
}
