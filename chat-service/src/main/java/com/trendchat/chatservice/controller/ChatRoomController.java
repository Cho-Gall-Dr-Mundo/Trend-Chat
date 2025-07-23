package com.trendchat.chatservice.controller;

import com.trendchat.chatservice.dto.*;
import com.trendchat.chatservice.service.ChatRoomService;
import com.trendchat.chatservice.service.SummarySseService;
import com.trendchat.trendchatcommon.auth.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final SummarySseService summarySseService;

    @GetMapping
    public ResponseEntity<List<ChatRoomListResponse>> getAllChatRooms() {
        return ResponseEntity.ok(chatRoomService.getAllChatRooms());
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

    // 일부 list & top5 list받으면 그것만 통계
    @PostMapping("/stats/bulk")
    public ResponseEntity<Map<Long, ChatRoomStatsResponse>> getRoomStats(@RequestBody List<Long> roomIds) {
        return ResponseEntity.ok(chatRoomService.getRoomStats(roomIds));
    }

    // 전체 방 통계
    @GetMapping("/stats/all")
    public ResponseEntity<Map<Long, ChatRoomStatsResponse>> getAllStats() {
        return ResponseEntity.ok(chatRoomService.getAllRoomStats());
    }

    // 상위 5개 방 ID
    @GetMapping("/stats/top5")
    public ResponseEntity<List<Long>> getTop6RoomIds() {
        return ResponseEntity.ok(chatRoomService.getTop6ActiveRoomIds());
    }

    //구독중 채티방 목록
    @GetMapping("/my")
    public ResponseEntity<List<MyRoomResponse>> getMyRooms(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(chatRoomService.getMyRooms(authUser.getUserId()));
    }

    //뉴메세지 알림
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<RoomSummaryEvent>> subscribe(@AuthenticationPrincipal AuthUser authUser) {
        return summarySseService.subscribe(authUser.getUserId());
    }
}
