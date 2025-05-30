package com.trendchat.chatservice.repository;

import com.trendchat.chatservice.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    boolean existsByTitle(String title);
}
