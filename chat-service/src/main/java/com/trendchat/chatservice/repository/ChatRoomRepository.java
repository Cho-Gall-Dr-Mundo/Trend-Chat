package com.trendchat.chatservice.repository;

import com.trendchat.chatservice.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    boolean existsByTitle(String title);

    @Query("""
    SELECT DISTINCT r FROM ChatRoom r
    LEFT JOIN FETCH r.messages
    WHERE r.id = :roomId
""")
    Optional<ChatRoom> findByIdWithMessages(@Param("roomId") Long roomId);

    @Query("""
    SELECT DISTINCT r FROM ChatRoom r
    LEFT JOIN FETCH r.messages
    WHERE r.title = :title
""")
    Optional<ChatRoom> findByTitleWithMessages(@Param("title") String title);
}
