package com.trendchat.chatservice.service;

import com.trendchat.chatservice.dto.RoomSummaryEvent;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.util.List;

public interface SummarySseService {
    Flux<ServerSentEvent<RoomSummaryEvent>> subscribe(String userId);
    void pushToSubscribers(RoomSummaryEvent event);
}
