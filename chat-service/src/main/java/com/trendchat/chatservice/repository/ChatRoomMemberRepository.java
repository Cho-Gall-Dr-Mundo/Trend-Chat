package com.trendchat.chatservice.repository;

import com.trendchat.chatservice.entity.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    List<ChatRoomMember> findByChatRoomId(Long roomdId);
    boolean existsByChatRoomIdAndUserId(Long roomId, String userId);
    int countByChatRoomId(Long roomId);
    @Query("select m.userId from ChatRoomMember m where m.chatRoom.id = :roomId")
    List<String> findUserIdsByRoomId(@Param("roomId") Long roomId);
    @Query("SELECT COUNT(m) FROM ChatRoomMember m WHERE m.userId = :userId")
    long countUserSubscriptions(@Param("userId") String userId);
}
