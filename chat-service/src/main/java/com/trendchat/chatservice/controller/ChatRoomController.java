package com.trendchat.chatservice.controller;

import com.trendchat.chatservice.dto.ChatRoomRequest;
import com.trendchat.chatservice.entity.ChatRoom;
import com.trendchat.chatservice.service.ChatRoomService;
import com.trendchat.chatservice.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping
    public ResponseEntity<ChatRoom> createChatRoom(@RequestBody ChatRoomRequest request) {
        ChatRoom chatRoom = chatRoomService.createChatRoom(request.title(), request.description());
        return ResponseEntity.ok(chatRoom);
    }

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
