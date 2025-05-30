package com.trendchat.chatservice.controller;

import com.trendchat.chatservice.entity.ChatRoom;
import com.trendchat.chatservice.service.ChatRoomService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @GetMapping
    public List<ChatRoom> getAllChatRooms() {
        return chatRoomService.getAllChatRooms();
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ChatRoom> getChatRoomById(@PathVariable Long roomId) {
        ChatRoom chatRoom = chatRoomService.getChatRoomById(roomId);
        return ResponseEntity.ok(chatRoom);
    }
}
