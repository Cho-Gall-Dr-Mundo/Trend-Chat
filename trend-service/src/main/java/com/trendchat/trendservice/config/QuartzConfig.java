package com.trendchat.trendservice.config;

import com.trendchat.trendservice.job.GoogleTrendsJob;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Quartz 스케줄러의 작업 등록을 위한 설정 클래스입니다.
 * <p>
 * 이 클래스는 {@link GoogleTrendsJob}을 Quartz Job으로 등록하고, 해당 Job을 1분 주기로 실행하는 {@link Trigger}를 구성합니다.
 * </p>
 *
 * <ul>
 *     <li>{@link #trendsJobDetail()} — 실행할 작업(Job)의 정의</li>
 *     <li>{@link #trendsJobTrigger()} — 작업의 실행 주기를 정의하는 트리거</li>
 * </ul>
 */
@Configuration
public class QuartzConfig {

    /**
     * Quartz 스케줄러에 등록할 {@link GoogleTrendsJob}의 JobDetail Bean을 생성합니다.
     * <p>
     * 이 JobDetail은 "trendsJob"이라는 식별자를 가지며, durable(내구성 저장)이 활성화되어 Trigger 없이도 Job 정보를 유지할 수 있습니다.
     * </p>
     *
     * @return {@link GoogleTrendsJob}을 정의하는 Quartz JobDetail 객체
     */
    @Bean
    public JobDetail trendsJobDetail() {
        return JobBuilder.newJob(GoogleTrendsJob.class)
                .withIdentity("trendsJob")
                .storeDurably()
                .build();
    }

    /**
     * {@link com.trendchat.trendservice.job.GoogleTrendsJob}을 1분마다 실행하도록 설정된 Quartz Trigger Bean을
     * 생성합니다.
     * <p>
     * 이 트리거는 {@code trendsJobDetail()}에 의해 정의된 Job과 연계되며, "trendsTrigger"라는 식별자를 갖고 매 1분마다 반복 실행되도록
     * 구성됩니다.
     * </p>
     *
     * @return Google Trends 수집 작업을 주기적으로 실행하는 Quartz Trigger 객체
     */
    @Bean
    public Trigger trendsJobTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(trendsJobDetail())
                .withIdentity("trendsTrigger")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(1)
                        .repeatForever())
                .build();
    }
}