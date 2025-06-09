package com.trendchat.chatservice.service;

import com.trendchat.chatservice.dto.ChatMessageRequest;
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

    @Override
    @Transactional
    public ChatMessage saveMessage(ChatMessage message){
        return repository.save(message);
    }

    @Override
    public List<ChatMessage> getMessageHistory(Long roomId){
        return repository.findByChatRoomIdOrderByTimestampAsc(roomId);
    }

    @Override
    public void handleMessage(ChatMessageRequest messageRequest) {
        // 1. 채팅방 ID로 ChatRoom 엔티티 찾기
        ChatRoom chatRoom = chatRoomRepository.findById(Long.valueOf(messageRequest.roomId())).orElseThrow(()-> new IllegalArgumentException("Room not found"));

        // 2. ChatMessage 생성 & 채팅방 연관 설정
        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(messageRequest.sender())
                .content(messageRequest.content())
                .build();
        publisher.send(message);
    }
}
