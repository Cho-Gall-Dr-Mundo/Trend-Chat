package com.trendchat.chatservice.controller;

import com.trendchat.chatservice.dto.ChatMessageRequest;
import com.trendchat.chatservice.dto.ChatMessageResponse;
import com.trendchat.chatservice.entity.ChatMessage;
import com.trendchat.chatservice.service.ChatService;
import com.trendchat.trendchatcommon.auth.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatController {
    private final ChatService chatService;
    private final Sinks.Many<ChatMessage> sink;

    // 클라이언트 → 서버로 메시지 전송 (MQ 발행)
    @PostMapping("/send")
    public void send(@RequestBody ChatMessageRequest messageRequest) {
        chatService.handleMessage(messageRequest);
    }

    // 클라이언트가 실시간 채팅 메시지를 수신 (SSE)
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatMessage> stream() {
        return sink.asFlux();
    }

    // 과거 메시지 조회 API (roomId 기준)
    @GetMapping("/history/{roomId}")
    public List<ChatMessageResponse> getMessageHistory(@PathVariable Long roomId, @AuthenticationPrincipal AuthUser authUser) {
        return chatService.getMessageHistory(roomId, authUser.getUserId());
    }
}
