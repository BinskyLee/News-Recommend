package com.fzu.recommend.quartz;

import com.fzu.recommend.service.RecommendService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class UserTagJob implements Job {

    @Autowired
    private RecommendService recommendService;

    private static final Logger logger = LoggerFactory.getLogger(UserTagJob.class);


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info("任务开始,计算用户标签");
        recommendService.calUserTag();
        logger.info("任务结束,用户标签计算完毕");
    }
}
