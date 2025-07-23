package com.trendchat.trendservice.config;

import com.trendchat.trendservice.job.GoogleTrendsCrawlJob;
import com.trendchat.trendservice.job.HotKeywordDetectJob;
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
 * 이 클래스는 트렌드 키워드 관련 작업들을 Quartz Job으로 등록하고, 각 작업을 1분 주기로 실행하도록 설정합니다.
 * </p>
 *
 * <h2>등록된 작업(Job)</h2>
 * <ul>
 *     <li>{@link com.trendchat.trendservice.job.GoogleTrendsCrawlJob}:
 *         Google Trends에서 트렌드 키워드를 수집하여 DB와 Kafka로 전송</li>
 *     <li>{@link com.trendchat.trendservice.job.HotKeywordDetectJob}:
 *         Redis에 저장된 키워드 데이터를 분석하여 급상승 키워드(HOT)를 감지</li>
 * </ul>
 *
 * <h2>스케줄링 주기</h2>
 * <ul>
 *     <li>두 작업 모두 1분마다 실행되도록 Quartz Trigger로 구성</li>
 * </ul>
 *
 * @see com.trendchat.trendservice.job.GoogleTrendsCrawlJob
 * @see com.trendchat.trendservice.job.HotKeywordDetectJob
 */
@Configuration
public class QuartzConfig {

    /**
     * {@link GoogleTrendsCrawlJob}의 JobDetail Bean을 생성합니다.
     * <p>
     * 트렌드 키워드 크롤링 및 저장/전송 작업을 정의합니다.
     * </p>
     *
     * @return GoogleTrendsCrawlJob의 Quartz JobDetail
     */
    @Bean
    public JobDetail trendsJobDetail() {
        return JobBuilder.newJob(GoogleTrendsCrawlJob.class)
                .withIdentity("trendsJob")
                .storeDurably()
                .build();
    }

    /**
     * {@link GoogleTrendsCrawlJob}을 1분마다 실행하도록 설정된 Quartz Trigger를 생성합니다.
     *
     * @return Google Trends 수집 Job에 대한 Trigger
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

    /**
     * {@link HotKeywordDetectJob}의 JobDetail Bean을 생성합니다.
     * <p>
     * Redis 기반의 급상승 키워드 감지 작업을 정의합니다.
     * </p>
     *
     * @return HotKeywordDetectJob의 Quartz JobDetail
     */
    @Bean
    public JobDetail hotKeywordDetectJobDetail() {
        return JobBuilder.newJob(HotKeywordDetectJob.class)
                .withIdentity("hotKeywordDetectJob")
                .storeDurably()
                .build();
    }

    /**
     * {@link HotKeywordDetectJob}을 1분마다 실행하도록 설정된 Quartz Trigger를 생성합니다.
     *
     * @return HOT 키워드 감지 Job에 대한 Trigger
     */
    @Bean
    public Trigger hotKeywordDetectJobTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(hotKeywordDetectJobDetail())
                .withIdentity("hotKeywordDetectTrigger")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(1)
                        .repeatForever())
                .build();
    }
}