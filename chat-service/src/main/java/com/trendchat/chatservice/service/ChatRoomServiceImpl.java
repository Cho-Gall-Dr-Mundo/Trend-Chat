package com.trendchat.chatservice.service;

import com.trendchat.chatservice.entity.ChatRoom;
import com.trendchat.chatservice.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;

    @Override
    public ChatRoom createChatRoom(String title, String description) {
        return chatRoomRepository.save(ChatRoom.builder()
                .title(title)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Override
    public List<ChatRoom> getAllChatRooms() {
        return chatRoomRepository.findAll();
    }

    @Override
    public ChatRoom getChatRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId).orElseThrow(()-> new IllegalArgumentException("Room not found"));
    }
}
