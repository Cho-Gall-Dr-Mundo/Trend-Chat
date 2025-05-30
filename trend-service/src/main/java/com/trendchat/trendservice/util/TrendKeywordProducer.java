package com.trendchat.trendservice.util;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrendKeywordProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String TOPIC = "trend-keywords";

    public void send(String key, Object value) {
        kafkaTemplate.send(TOPIC, key, value);
    }
}