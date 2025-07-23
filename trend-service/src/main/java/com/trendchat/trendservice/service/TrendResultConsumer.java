package com.trendchat.trendservice.service;

import com.trendchat.trendservice.dto.TrendItem;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@code TrendResultConsumer}는 Kafka의 {@code trend-results} 토픽으로부터 트렌드 분석 결과를 수신하여 처리하는 이벤트 소비자 역할을
 * 수행합니다.
 *
 * <p>수신된 메시지는 {@link TrendService#processTrendResult(TrendItem)}를 통해 DB에 반영됩니다.</p>
 *
 * <p>Kafka 메시지 처리 중 예외가 발생하면 트랜잭션 롤백 및 재처리가 자동으로 이뤄집니다.</p>
 *
 * <p><b>관련 Kafka 설정:</b></p>
 * <ul>
 *   <li>Topic: {@code trend-results}</li>
 *   <li>Group ID: {@code trend-service}</li>
 * </ul>
 *
 * @author TrendChat
 */
@Component
@RequiredArgsConstructor
public class TrendResultConsumer {

    private final TrendService trendService;

    /**
     * Kafka로부터 {@code TrendItem} 메시지를 수신하고 트렌드 처리 로직을 호출합니다.
     *
     * @param item 수신된 트렌드 분석 결과
     */
    @Transactional
    @KafkaListener(topics = "trend-results", groupId = "trend-service")
    public void handleTrendResult(TrendItem item) {
        trendService.processTrendResult(item);
    }
}
