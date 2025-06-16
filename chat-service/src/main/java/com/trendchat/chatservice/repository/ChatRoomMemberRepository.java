package com.trendchat.chatservice.repository;

import com.trendchat.chatservice.entity.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    List<ChatRoomMember> findByChatRoomId(Long roomdId);
    boolean existsByChatRoomIdAndUserId(Long roomId, String userId);
}
