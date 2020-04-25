package com.fzu.recommend.quartz;

import com.fzu.recommend.service.RecommendService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class UserSimJob implements Job {

    @Autowired
    private RecommendService recommendService;

    private static final Logger logger = LoggerFactory.getLogger(UserSimJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // 更新用户相似度

        logger.info("任务开始,正在计算用户相似度");
        recommendService.calUserSim();
        logger.info("任务结束, 用户相似度计算完毕");

    }
}
