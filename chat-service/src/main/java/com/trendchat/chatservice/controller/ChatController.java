package com.trendchat.chatservice.controller;

import com.trendchat.chatservice.dto.ChatMessageRequest;
import com.trendchat.chatservice.dto.ChatMessageResponse;
import com.trendchat.chatservice.service.ChatMessageConsumer;
import com.trendchat.chatservice.service.ChatService;
import com.trendchat.trendchatcommon.auth.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatController {
    private final ChatService chatService;
    private final ChatMessageConsumer chatMessageConsumer;

    // 클라이언트 → 서버로 메시지 전송 (MQ 발행)
    @PostMapping("/send")
    public void send(@RequestBody ChatMessageRequest messageRequest, @AuthenticationPrincipal AuthUser authUser) {
        ChatMessageRequest fullMessageRequest = ChatMessageRequest.builder()
                .roomId(messageRequest.roomId())
                .sender(authUser.getUserId())
                .senderNickName(authUser.getNickname())
                .content(messageRequest.content())
                .build();
        chatService.handleMessage(fullMessageRequest);
    }

    // 클라이언트가 실시간 채팅 메시지를 수신 (SSE)
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatMessageResponse> stream() {
        return chatMessageConsumer.getStream();
    }

    // 과거 메시지 조회 API (roomId 기준)
    @GetMapping("/history/{roomId}")
    public List<ChatMessageResponse> getMessageHistory(@PathVariable Long roomId, @AuthenticationPrincipal AuthUser authUser) {
        return chatService.getMessageHistory(roomId, authUser.getUserId());
    }
}
