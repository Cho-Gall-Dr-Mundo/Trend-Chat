package com.trendchat.chatservice.service;

import com.trendchat.chatservice.dto.TrendItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrendKeywordConsumer {

    private final ChatRoomService chatRoomService;

    @KafkaListener(topics = "trend-keywords", groupId = "chat-service")
    public void consume(
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Payload(required = false) TrendItem value
    ) {
        log.info("Received trending keyword: {}", key);
        String description = key + "에 대한 최근 이슈를 기반으로 사용자들이 대화 중입니다.";

        if (chatRoomService.createChatRoom(key, description)) {
            log.info("ChatRoom created for trending keyword: {}", key);
        }
    }
}
