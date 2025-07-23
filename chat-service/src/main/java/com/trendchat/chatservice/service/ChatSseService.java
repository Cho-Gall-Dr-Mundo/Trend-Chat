package com.trendchat.chatservice.service;

import com.trendchat.chatservice.dto.ChatMessageResponse;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

public interface ChatSseService {
    Flux<ServerSentEvent<ChatMessageResponse>> subscribe(String userId);
    void broadcast(ChatMessageResponse message);
}
