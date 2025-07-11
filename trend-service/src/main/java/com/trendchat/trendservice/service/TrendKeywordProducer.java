package com.trendchat.trendservice.service;

import com.trendchat.trendservice.dto.TrendKeywordItem;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrendKeywordProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(String key, TrendKeywordItem value) {
        kafkaTemplate.send("trend-keywords", key, value);
    }
}