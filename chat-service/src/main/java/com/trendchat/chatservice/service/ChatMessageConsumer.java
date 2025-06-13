package com.trendchat.chatservice.service;

import com.trendchat.chatservice.config.RabbitMQConfig;
import com.trendchat.chatservice.dto.ChatMessageDto;
import com.trendchat.chatservice.dto.ChatMessageResponse;
import com.trendchat.chatservice.entity.ChatMessage;
import com.trendchat.chatservice.entity.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ChatMessageConsumer {

    private final ChatSseService chatSseService;
    private final ChatService chatService;
    private final ChatRoomService chatRoomService;

    /**
     *  RabbitMQ로부터 메시지 수신 시 자동 실행되는 메서드
     *  - MQ에서 ChatMessageDto를 받아서,
     *  - DB에 저장하고,
     *  - 모든 SSE 구독자에게 메시지를 전송함
     */
    @RabbitListener(queues = RabbitMQConfig.CHAT_QUEUE)
    @Transactional
    public void receive(ChatMessageDto dto) {
        //채팅방 조회
        ChatRoom room = chatRoomService.getChatRoomById(dto.roomId());

        //ChatMessage 엔티티 생성 및 DB 저장
        ChatMessage message = ChatMessage.builder()
                .chatRoom(room)
                .sender(dto.sender())
                .content(dto.content())
                .timestamp(LocalDateTime.now())
                .build();

        chatService.saveMessage(message);

        //SSE 클라이언트에게 보낼 응답 객체 생성
        ChatMessageResponse response = new ChatMessageResponse(
                message.getId(),
                dto.roomId(),
                dto.senderNickname(),
                message.getContent(),
                message.getTimestamp(),
                false // 기본값: SSE 구독자 기준 isMine은 프론트에서 판단
        );

        //실시간으로 모든 구독자에게 메시지 전송
        chatSseService.broadcast(response);
    }
}
