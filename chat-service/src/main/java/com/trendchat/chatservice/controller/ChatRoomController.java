package com.trendchat.chatservice.controller;

import com.trendchat.chatservice.dto.ChatRoomResponse;
import com.trendchat.chatservice.entity.ChatRoom;
import com.trendchat.chatservice.service.ChatRoomService;
import java.util.List;

import com.trendchat.trendchatcommon.auth.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<ChatRoomResponse> getChatRoomById(@PathVariable Long roomId, @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(chatRoomService.getChatRoomByIdResponse(roomId, authUser.getUserId()));
    }

    // 프론트에서 title만 넣으면 자동 조회/생성
    @GetMapping("/title/{title}")
    public ResponseEntity<ChatRoomResponse> getOrCreateByTitle(@PathVariable String title, @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(chatRoomService.getOrCreateByTitle(title, authUser.getUserId()));
    }
}
