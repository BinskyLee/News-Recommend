package com.fzu.recommend.config;


import com.fzu.recommend.quartz.NewsScoreRefreshJob;
import com.fzu.recommend.quartz.UserSimJob;
import com.fzu.recommend.quartz.UserTagJob;
import com.fzu.recommend.quartz.ViewCountSaveJob;
import com.fzu.recommend.recommend.UserTag;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Configuration
public class QuartzConfig {

    private static final Date start;

    static{
        try {
            start = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2020-1-21 00:00:00");
        } catch (ParseException e) {
            throw  new RuntimeException("初始化起始日失败" + e.getMessage());
        }
    }

    // 刷新文章分数任务
    @Bean
    public JobDetailFactoryBean newsScoreRefreshJobDetail(){
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(NewsScoreRefreshJob.class);
        factoryBean.setName("NewsScoreRefreshJob");
        factoryBean.setGroup("recommendJobGroup");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    @Bean
    public SimpleTriggerFactoryBean newsScoreRefreshTrigger(JobDetail newsScoreRefreshJobDetail){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(newsScoreRefreshJobDetail);
        factoryBean.setName("newsScoreRefreshTrigger");
        factoryBean.setGroup("recommendTriggerGroup");
        factoryBean.setRepeatInterval(1000 * 60 * 5);
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }


    // 定期存储点击量
    @Bean
    public JobDetailFactoryBean viewCountSaveJobDetail(){
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(ViewCountSaveJob.class);
        factoryBean.setName("ViewCountSaveJob");
        factoryBean.setGroup("recommendJobGroup");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }


    @Bean
    public SimpleTriggerFactoryBean viewCountSaveTrigger(JobDetail viewCountSaveJobDetail){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(viewCountSaveJobDetail);
        factoryBean.setName("viewCountSaveTrigger");
        factoryBean.setGroup("recommendTriggerGroup");
        factoryBean.setStartTime(start);
        factoryBean.setRepeatInterval(1000 * 60 * 60 *24);
//        factoryBean.setRepeatInterval(1000 * 60 * 10);
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }

    //  计算用户相似度
    @Bean
    public JobDetailFactoryBean userSimJobDetail(){
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(UserSimJob.class);
        factoryBean.setName("UserSimJob");
        factoryBean.setGroup("recommendJobGroup");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }


    @Bean
    public SimpleTriggerFactoryBean userSimTrigger(JobDetail userSimJobDetail){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(userSimJobDetail);
        factoryBean.setName("userSimSaveTrigger");
        factoryBean.setGroup("recommendTriggerGroup");
        factoryBean.setStartTime(start);
        factoryBean.setRepeatInterval(1000 * 60 * 60 *24);
//        factoryBean.setRepeatInterval(1000 * 60 * 10);
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }

    @Bean
    public JobDetailFactoryBean userTagJobDetail(){
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(UserTagJob.class);
        factoryBean.setName("UserTagJob");
        factoryBean.setGroup("recommendJobGroup");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }


    @Bean
    public SimpleTriggerFactoryBean userTagTrigger(JobDetail userTagJobDetail){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(userTagJobDetail);
        factoryBean.setName("userTagTrigger");
        factoryBean.setGroup("recommendTriggerGroup");
        factoryBean.setStartTime(start);
        factoryBean.setRepeatInterval(1000 * 60 * 60 *24);
//        factoryBean.setRepeatInterval(1000 * 60 * 10);
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }
}
