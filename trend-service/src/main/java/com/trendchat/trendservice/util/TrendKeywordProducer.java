package com.trendchat.trendservice.util;

import com.trendchat.trendservice.dto.TrendItem;
import com.trendchat.trendservice.dto.TrendKeywordItem;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * {@code TrendKeywordProducer}는 Kafka를 통해 트렌드 키워드 관련 메시지를 발행하는 프로듀서 컴포넌트입니다.
 *
 * <p>트렌드 키워드 또는 상세 트렌드 데이터를 다른 마이크로서비스에 전파하는 데 사용됩니다.</p>
 *
 * <p>발행되는 주요 토픽:</p>
 * <ul>
 *   <li><b>trend-keywords</b> – 키워드 및 유입량 기반의 간략한 트렌드 정보 전파</li>
 *   <li><b>trend-created</b> – 분류 및 요약이 포함된 트렌드 생성/업데이트 정보 전달</li>
 * </ul>
 *
 * @author TrendChat
 */
@Component
@RequiredArgsConstructor
public class TrendKeywordProducer {

    /**
     * Kafka 메시지 발행용 템플릿
     */
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * {@code trend-keywords} 토픽으로 키워드 정보를 발행합니다.
     *
     * @param key   트렌드 키워드
     * @param value 유입량 등의 간단한 키워드 정보
     */
    public void sendKeyword(String key, TrendKeywordItem value) {
        kafkaTemplate.send("trend-keywords", key, value);
    }

    /**
     * {@code trend-created} 토픽으로 트렌드 분석 결과 전체를 발행합니다.
     *
     * @param key   트렌드 키워드
     * @param value 요약, 카테고리 포함된 전체 트렌드 정보
     */
    public void sendTrend(String key, TrendItem value) {
        kafkaTemplate.send("trend-created", key, value);
    }
}
