package com.trendchat.trendservice.job;

import com.trendchat.trendservice.dto.TrendKeywordItem;
import com.trendchat.trendservice.service.TrendKeywordServiceImpl;
import com.trendchat.trendservice.util.GoogleTrendsCrawler;
import com.trendchat.trendservice.util.TrendKeywordProducer;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

/**
 * Google Trends 데이터를 주기적으로 수집하여 DB에 저장하고 Kafka로 전송하는 Quartz Job 클래스입니다.
 * <p>
 * 이 클래스는 Quartz 스케줄러에 의해 1분 주기로 실행되며, 내부적으로
 * {@link com.trendchat.trendservice.util.GoogleTrendsCrawler}를 이용해 트렌드 키워드를 크롤링한 후:
 * </p>
 * <ul>
 *     <li>{@link TrendKeywordServiceImpl}를 통해 키워드를 DB에 저장하고</li>
 *     <li>{@link com.trendchat.trendservice.util.TrendKeywordProducer}를 통해 Kafka 토픽("trend-keywords")으로 전송합니다.</li>
 * </ul>
 *
 * @see com.trendchat.trendservice.util.GoogleTrendsCrawler
 * @see TrendKeywordServiceImpl
 * @see com.trendchat.trendservice.util.TrendKeywordProducer
 * @see com.trendchat.trendservice.config.QuartzConfig
 */
@Component
@RequiredArgsConstructor
public class GoogleTrendsCrawlJob implements Job {

    private final TrendKeywordProducer trendKeywordProducer;
    private final TrendKeywordServiceImpl trendKeywordService;
    private final GoogleTrendsCrawler googleTrendsCrawler;

    /**
     * Quartz 스케줄러에 의해 주기적으로 실행되는 작업으로, Google Trends에서 트렌드 키워드를 수집하여 DB에 저장하고 Kafka로도 전송합니다.
     * <p>
     * 1. {@link GoogleTrendsCrawler}로 크롤링된 키워드를<br> 2. {@link TrendKeywordServiceImpl}를 통해 DB에
     * 저장하고,<br> 3. {@link TrendKeywordProducer}를 통해 Kafka로 전송합니다.
     * </p>
     *
     * @param jobExecutionContext Quartz가 제공하는 실행 컨텍스트 객체 (현재 사용하지 않음)
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        Map<String, TrendKeywordItem> trendItemMap = googleTrendsCrawler.crawl();

        for (Map.Entry<String, TrendKeywordItem> entry : trendItemMap.entrySet()) {
            trendKeywordService.createTrendKeyword(
                    entry.getKey(),
                    entry.getValue().approxTraffic()
            );
            trendKeywordProducer.send(entry.getKey(), entry.getValue());
        }
    }
}
