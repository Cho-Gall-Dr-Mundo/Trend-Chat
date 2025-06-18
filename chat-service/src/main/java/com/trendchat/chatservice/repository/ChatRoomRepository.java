package com.trendchat.chatservice.repository;

import com.trendchat.chatservice.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
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

    Optional<ChatRoom> findByTitle(String title);

    @Query("SELECT r.id FROM ChatRoom r")
    List<Long> findAllRoomIds();

    @Query("""
        SELECT m.chatRoom.id, COUNT(m)
        FROM ChatMessage m
        WHERE m.timestamp > :after
        GROUP BY m.chatRoom.id
        ORDER BY COUNT(m) DESC
        LIMIT 6
    """)
    List<Object[]> findTop6ActiveRooms(@Param("after") LocalDateTime after);
}
