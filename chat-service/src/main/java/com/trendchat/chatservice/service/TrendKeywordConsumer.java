package com.trendchat.chatservice.service;

import com.trendchat.chatservice.dto.TrendItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * {@code TrendKeywordConsumer}는 Kafka의 {@code trend-created} 토픽으로부터 트렌드 분석 결과를 수신하여, 해당 키워드에 대한
 * 채팅방을 생성하는 역할을 수행하는 이벤트 소비자입니다.
 *
 * <p>이 클래스는 {@code chat-service}의 일원으로, Kafka 기반의 비동기 이벤트를 처리하여
 * 사용자 액션 없이도 트렌드 기반의 채팅방이 자동으로 생성되도록 합니다.</p>
 *
 * <p>EDA(Event-Driven Architecture)의 소비자 역할을 하며, 수신된 {@link TrendItem} 데이터를 기반으로
 * {@link ChatRoomService}를 통해 채팅방을 생성합니다.</p>
 *
 * @author TrendChat
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrendKeywordConsumer {

    private final ChatRoomService chatRoomService;

    /**
     * {@code trend-created} Kafka 토픽으로부터 트렌드 데이터를 수신하여, 해당 키워드를 기반으로 채팅방을 생성합니다.
     *
     * @param key   트렌드 키워드 (Kafka 메시지 키)
     * @param value 트렌드 상세 정보 (요약 포함), null 허용
     */
    @KafkaListener(topics = "trend-created", groupId = "chat-service")
    public void consume(
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Payload(required = false) TrendItem value
    ) {
        if (chatRoomService.createChatRoom(key, value.summary())) {
            log.info("ChatRoom created for trending keyword: {}", key);
        }
    }
}