package com.trendchat.chatservice.repository;

import com.trendchat.chatservice.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomIdOrderByTimestampAsc(Long roomId);
    int countByChatRoomIdAndTimestampAfter(Long roomId, LocalDateTime timestamp);
}
