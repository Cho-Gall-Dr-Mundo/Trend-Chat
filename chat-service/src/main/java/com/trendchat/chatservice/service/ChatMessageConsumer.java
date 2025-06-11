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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ChatMessageConsumer {
    private final Sinks.Many<ChatMessageResponse> sink = Sinks.many().multicast().onBackpressureBuffer() ; //WebFlux SSE전송
    private final ChatService chatService;
    private final ChatRoomService chatRoomService;

    // MQ로부터 메시지를 수신하면 자동 실행됨
    @RabbitListener(queues = RabbitMQConfig.CHAT_QUEUE)
    @Transactional
    public void receive(ChatMessageDto dto) {
        // 1. 채팅방 조회
        ChatRoom chatRoom = chatRoomService.getChatRoomById(dto.roomId());

        // 2. 메시지 생성 및 저장
        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom) // 주입 or 조회
                .sender(dto.sender())
                .content(dto.content())
                .timestamp(LocalDateTime.now()) // 또는 dto.timestamp() 파싱
                .build();

        chatService.saveMessage(message);

        // 3. SSE응답용 DTO생성
        ChatMessageResponse response = new ChatMessageResponse(
                message.getId(),
                dto.senderNickname(),
                message.getContent(),
                message.getTimestamp(),
                false
        );

        // 4.실시간 전송
        sink.tryEmitNext(response);
    }

    //SSE 구독 API에서 사용할 수 있게 공개
    public Flux<ChatMessageResponse> getStream(){
        return sink.asFlux();
    }
}
