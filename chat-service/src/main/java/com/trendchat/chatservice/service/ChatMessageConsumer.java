package com.trendchat.chatservice.service;

import com.trendchat.chatservice.config.RabbitMQConfig;
import com.trendchat.chatservice.dto.ChatMessageDto;
import com.trendchat.chatservice.entity.ChatMessage;
import com.trendchat.chatservice.entity.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Sinks;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ChatMessageConsumer {
    private final Sinks.Many<ChatMessage> sink; //WebFlux SSE전송
    private final ChatService chatService;
    private final ChatRoomService chatRoomService;

    // MQ로부터 메시지를 수신하면 자동 실행됨
    @RabbitListener(queues = RabbitMQConfig.CHAT_QUEUE)
    @Transactional
    public void receive(ChatMessageDto dto) {
        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoomService.getChatRoomById(dto.roomId())) // 주입 or 조회
                .sender(dto.sender())
                .content(dto.content())
                .timestamp(LocalDateTime.now()) // 또는 dto.timestamp() 파싱
                .build();

        chatService.saveMessage(message);
        sink.tryEmitNext(message);
    }
}
