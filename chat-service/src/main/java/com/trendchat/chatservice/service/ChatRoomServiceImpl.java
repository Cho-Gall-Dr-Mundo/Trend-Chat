package com.trendchat.chatservice.service;

import com.trendchat.chatservice.dto.ChatMessageResponse;
import com.trendchat.chatservice.dto.ChatRoomResponse;
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
    public ChatRoomResponse getChatRoomByIdResponse(Long roomId, String currentUserId) {
        ChatRoom chatRoom = chatRoomRepository.findByIdWithMessages(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        return toChatRoomResponse(chatRoom, currentUserId);
    }

    @Override
    public ChatRoom getChatRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
    }

    private boolean isExistsTitle(String title) {
        return chatRoomRepository.existsByTitle(title);
    }

    @Override
    public ChatRoomResponse getOrCreateByTitle(String title, String currentUserId) {
        ChatRoom chatRoom = chatRoomRepository.findByTitleWithMessages(title)
                .orElseGet(() -> chatRoomRepository.save(ChatRoom.builder()
                        .title(title)
                        .description(title + "에 대한 자동 생성된 채팅방")
                        .createdAt(LocalDateTime.now())
                        .build()));
        return toChatRoomResponse(chatRoom, currentUserId);
    }

    private ChatRoomResponse toChatRoomResponse(ChatRoom chatRoom, String currentUserId) {
        List<ChatMessageResponse> messages = chatRoom.getMessages().stream()
                .map(msg -> new ChatMessageResponse(
                        msg.getId(),
                        msg.getChatRoom().getId(),
                        msg.getSender(),
                        msg.getSenderNickname(),
                        msg.getContent(),
                        msg.getTimestamp(),
                        msg.getSender().equals(currentUserId)
                ))
                .toList();

        return ChatRoomResponse.builder()
                .id(chatRoom.getId())
                .title(chatRoom.getTitle())
                .description(chatRoom.getDescription())
                .createdAt(chatRoom.getCreatedAt())
                .messages(messages)
                .build();
    }
}

