package com.trendchat.chatservice.service;

import com.trendchat.chatservice.config.RabbitMQConfig;
import com.trendchat.chatservice.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

@Component
@RequiredArgsConstructor
public class ChatMessageConsumer {
    private final Sinks.Many<ChatMessage> sink; //WebFlux SSE전송
    private final ChatService chatService;

    // MQ로부터 메시지를 수신하면 자동 실행됨
    @RabbitListener(queues = RabbitMQConfig.CHAT_QUEUE)
    public void recieve(ChatMessage message) {
        chatService.saveMessage(message); // DB에 저장
        sink.tryEmitNext(message); // SSE로 전송
    }
}
