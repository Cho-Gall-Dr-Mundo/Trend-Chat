package com.trendchat.chatservice.service;

import com.trendchat.chatservice.entity.ChatRoom;
import com.trendchat.chatservice.repository.ChatRoomRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    @Override
    @Transactional
    public boolean createChatRoom(String title, String description) {
        if (isExistsTitle(title)) {
            return false;
        }

        try {
            chatRoomRepository.save(ChatRoom.builder()
                    .title(title)
                    .description(description)
                    .createdAt(LocalDateTime.now())
                    .build());

            return true;
        } catch (DataIntegrityViolationException e) {
            // Unique 제약 위반 시
            return false;
        }
    }

    @Override
    public List<ChatRoom> getAllChatRooms() {
        return chatRoomRepository.findAll();
    }

    @Override
    public ChatRoom getChatRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
    }

    private boolean isExistsTitle(String title) {
        return chatRoomRepository.existsByTitle(title);
    }
}
