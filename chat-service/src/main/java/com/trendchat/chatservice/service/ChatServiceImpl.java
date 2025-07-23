package com.trendchat.chatservice.service;

import com.trendchat.chatservice.dto.ChatMessageDto;
import com.trendchat.chatservice.dto.ChatMessageRequest;
import com.trendchat.chatservice.dto.ChatMessageResponse;
import com.trendchat.chatservice.dto.RoomSummaryEvent;
import com.trendchat.chatservice.entity.ChatMessage;
import com.trendchat.chatservice.entity.ChatRoom;
import com.trendchat.chatservice.repository.ChatMessageRepository;
import com.trendchat.chatservice.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatServiceImpl implements ChatService{
    private final ChatMessageRepository repository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessagePublisher publisher;
    private final RedisPublisher redisPublisher;

    @Override
    @Transactional
    public ChatMessage saveMessage(ChatMessage message){
        return repository.save(message);
    }

    @Override
    public List<ChatMessageResponse> getMessageHistory(Long roomId, String currentUserEmail) {
        return repository.findByChatRoomIdOrderByTimestampAsc(roomId).stream()
                .map(msg -> new ChatMessageResponse(
                        msg.getId(),
                        msg.getChatRoom().getId(),
                        msg.getSender(),
                        msg.getSenderNickname(),
                        msg.getContent(),
                        msg.getTimestamp(),
                        msg.getSender().equals(currentUserEmail)
                ))
                .toList();
    }

    @Override
    public void handleMessage(ChatMessageRequest messageRequest) {
        // 1. 채팅방 ID로 ChatRoom 엔티티 찾기
        ChatRoom chatRoom = chatRoomRepository.findById(messageRequest.roomId()).orElseThrow(()-> new IllegalArgumentException("Room not found"));

        // 2. ChatMessage 생성 & 채팅방 연관 설정
        ChatMessageDto messageDto = new ChatMessageDto(
                chatRoom.getId(),
                messageRequest.sender(),
                messageRequest.senderNickName(),
                messageRequest.content()
        );
        // 3. 메시지 전송 (RabbitMQ → WebFlux 처리)
        publisher.send(messageDto);

        // 4. NEW 알림용 Redis Publish
        RoomSummaryEvent event = new RoomSummaryEvent(
                chatRoom.getId(),
                "NEW",
                LocalDateTime.now()
        );
        redisPublisher.publishRoomMessage(event);
    }
}
