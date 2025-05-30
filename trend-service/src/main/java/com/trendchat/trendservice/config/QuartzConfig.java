package com.trendchat.trendservice.config;

import com.trendchat.trendservice.job.TrendRssJob;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail rssJobDetail() {
        return JobBuilder.newJob(TrendRssJob.class)
                .withIdentity("trendRssJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger rssJobTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(rssJobDetail())
                .withIdentity("trendRssTrigger")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(1) // 1분마다 실행
                        .repeatForever())
                .build();
    }
}