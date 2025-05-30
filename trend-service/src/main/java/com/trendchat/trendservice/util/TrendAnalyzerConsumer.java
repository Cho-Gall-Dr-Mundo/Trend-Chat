package com.trendchat.trendservice.util;

import com.trendchat.trendservice.dto.NewsItem;
import com.trendchat.trendservice.dto.TrendItem;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrendAnalyzerConsumer {

    private final StringRedisTemplate redisTemplate;

    @KafkaListener(topics = "trend-keywords", groupId = "trend-analyzer")
    public void consume(
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Payload(required = false) TrendItem value
    ) {
        if (value == null || key == null || key.isBlank()) {
            log.warn("Received null or invalid payload. Skipping.");
            return;
        }

        long now = System.currentTimeMillis();

        // 1. 등장 횟수 증가 (ZSET)
        redisTemplate.opsForZSet().incrementScore("trend:rank:minute", key, 1);

        // 2. 상세 정보 저장 (HASH)
        String hashKey = "trend:detail:" + key;
        BoundHashOperations<String, Object, Object> hash = redisTemplate.boundHashOps(hashKey);

        hash.increment("count", 1);
        hash.increment("traffic", parseTraffic(value.approxTraffic()));
        hash.putIfAbsent("firstSeen", Instant.ofEpochMilli(now).toString());
        hash.put("lastSeen", Instant.ofEpochMilli(now).toString());

        // 3. 뉴스 출처 개수 저장 (중복 제거)
        Set<String> sources = value.newsItems().stream()
                .map(NewsItem::url).collect(Collectors.toSet());
        hash.put("newsSources", String.valueOf(sources.size()));

        log.info("Consumed and aggregated trending keyword: {}", key);
    }

    private long parseTraffic(String approxTraffic) {
        if (approxTraffic == null) {
            return 0;
        }
        try {
            return Long.parseLong(approxTraffic.replace("+", "").replace(",", ""));
        } catch (NumberFormatException e) {
            log.warn("Failed to parse traffic value: {}", approxTraffic);
            return 0;
        }
    }
}