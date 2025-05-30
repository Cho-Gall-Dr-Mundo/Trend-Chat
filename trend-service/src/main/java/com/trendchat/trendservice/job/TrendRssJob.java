package com.trendchat.trendservice.job;

import com.trendchat.trendservice.dto.TrendItem;
import com.trendchat.trendservice.service.RssTrendService;
import com.trendchat.trendservice.util.TrendKeywordProducer;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrendRssJob implements Job {

    private final RssTrendService rssTrendService;
    private final TrendKeywordProducer producer;

    @Override
    public void execute(JobExecutionContext context) {
        log.info("Fetching Google Trends RSS feed");

        Map<String, TrendItem> trends = rssTrendService.fetchTrends();

        trends.entrySet().stream()
                .limit(10)
                .forEach(entry -> {
                    String key = entry.getKey();           // title
                    TrendItem value = entry.getValue();    // no title inside

                    log.info("Publishing to Kafka — Key: {}, NewsItem count: {}",
                            key, value.newsItems().size());
                    producer.send(key, value);
                });
    }
}