package com.trendchat.chatservice.service;

import com.trendchat.chatservice.config.RabbitMQConfig;
import com.trendchat.chatservice.dto.ChatMessageDto;
import com.trendchat.chatservice.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMessagePublisher {

    private final RabbitTemplate rabbitTemplate; // RabbitMQ를 통한 메시지 전송을 위한 템플릿

    public void send(ChatMessage message) {
        ChatMessageDto dto = new ChatMessageDto(
                message.getChatRoom().getId(),
                message.getSender(),
                message.getContent()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CHAT_EXCHANGE,
                RabbitMQConfig.CHAT_ROUTING_KEY,
                dto
        );
    }
}