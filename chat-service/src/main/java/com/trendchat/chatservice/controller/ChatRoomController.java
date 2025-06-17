package com.trendchat.chatservice.controller;

import com.trendchat.chatservice.dto.ChatRoomResponse;
import com.trendchat.chatservice.entity.ChatRoom;
import com.trendchat.chatservice.service.ChatRoomService;
import java.util.List;
import java.util.Optional;

import com.trendchat.trendchatcommon.auth.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    // 조회 전용 (존재하는 채팅방만 조회)
    @GetMapping("/title/{title}")
    public ResponseEntity<ChatRoomResponse> getByTitle(@PathVariable String title, @AuthenticationPrincipal AuthUser authUser) {
        Optional<ChatRoomResponse> response = chatRoomService.findResponseByTitle(title, authUser.getUserId());

        return response
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // 생성 전용 (없으면 새로 생성)
    @PostMapping("/title/{title}")
    public ResponseEntity<ChatRoomResponse> createByTitle(@PathVariable String title, @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(chatRoomService.createByTitle(title, authUser.getUserId()));
    }

}
