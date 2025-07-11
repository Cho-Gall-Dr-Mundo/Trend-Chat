package com.trendchat.trendservice.service;

import com.trendchat.trendservice.dto.TrendItem;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TrendResultConsumer {

    private final TrendService trendService;

    @Transactional
    @KafkaListener(topics = "trend-results", groupId = "trend-service")
    public void handleTrendResult(TrendItem item) {
        trendService.processTrendResult(item);
    }
}
