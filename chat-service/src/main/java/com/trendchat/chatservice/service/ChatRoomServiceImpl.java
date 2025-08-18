package com.trendchat.chatservice.service;

import com.trendchat.chatservice.dto.*;
import com.trendchat.chatservice.entity.ChatRoom;
import com.trendchat.chatservice.repository.ChatMessageRepository;
import com.trendchat.chatservice.repository.ChatRoomMemberRepository;
import com.trendchat.chatservice.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;

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
    public List<ChatRoomListResponse> getAllChatRooms() {
        List<ChatRoom> chatRooms = chatRoomRepository.findAll();

        return chatRooms.stream().
                map(room -> ChatRoomListResponse.builder()
                        .id(room.getId())
                        .title(room.getTitle())
                        .description(room.getDescription())
                        .createdAt(room.getCreatedAt())
                .build())
                .toList();
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
    public Optional<ChatRoomResponse> findResponseByTitle(String title, String currentUserId) {
        return chatRoomRepository.findByTitleWithMessages(title)
                .map(room -> toChatRoomResponse(room, currentUserId));
    }


    @Override
    public ChatRoomResponse createByTitle(String title, String currentUserId) {
        ChatRoom existingRoom = chatRoomRepository.findByTitle(title).orElse(null);
        if (existingRoom != null) {
            throw new IllegalStateException("이미 존재하는 채팅방입니다.");
        }

        ChatRoom chatRoom = chatRoomRepository.save(ChatRoom.builder()
                .title(title)
                .description(title + "에 대한 자동 생성된 채팅방")
                .createdAt(LocalDateTime.now())
                .build());

        return toChatRoomResponse(chatRoom, currentUserId);
    }

    @Override
    public Map<Long, ChatRoomStatsResponse> getRoomStats(List<Long> roomIds) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);

        return roomIds.stream().collect(Collectors.toMap(
                roomId -> roomId,
                roomId -> {
                    int members = chatRoomMemberRepository.countByChatRoomId(roomId);
                    int messages = chatMessageRepository.countByChatRoomIdAndTimestampAfter(roomId, cutoff);
                    return ChatRoomStatsResponse.builder()
                            .participants(members)
                            .messageCount(messages)
                            .build();
                }
        ));
    }

    @Override
    public Map<Long, ChatRoomStatsResponse> getAllRoomStats() {
        List<Long> roomIds = chatRoomRepository.findAllRoomIds();
        return getRoomStats(roomIds);
    }

    @Override
    public List<Long> getTop6ActiveRoomIds() {
        LocalDateTime flag = LocalDateTime.now().minusMinutes(1440);
        List<Object[]> topRooms = chatRoomRepository.findTop6ActiveRooms(flag);

        return topRooms.stream()
                .map(row -> (Long) row[0])
                .collect(Collectors.toList());
    }

    @Override
    public List<MyRoomResponse> getMyRooms(String userId) {
        List<Long> roomIds = chatRoomRepository.findRoomIdsByUserId(userId);
        List<ChatRoom> rooms = chatRoomRepository.findAllById(roomIds);
        Map<Long, Long> participantCounts = chatRoomRepository.countByRoomIds(roomIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        return rooms.stream()
                .map(room -> new MyRoomResponse(
                        room.getId(),
                        room.getTitle(),
                        (long) participantCounts.getOrDefault(room.getId(), 0L).intValue()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public Long getTotalChatRooms(String userId) {
        List<Long> roomIds = chatRoomRepository.findRoomIdsByUserId(userId);
        return (long) roomIds.size();
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

