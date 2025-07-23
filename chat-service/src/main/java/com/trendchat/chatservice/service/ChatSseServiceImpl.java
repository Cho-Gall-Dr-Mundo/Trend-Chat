package com.trendchat.chatservice.service;

import com.trendchat.chatservice.dto.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ChatSseServiceImpl implements ChatSseService {
    //유저별 Sink관리
    private final Map<String, Sinks.Many<ChatMessageResponse>> userSinkMap = new ConcurrentHashMap<>();

    @Override
    public Flux<ServerSentEvent<ChatMessageResponse>> subscribe(String userId) {
        Sinks.Many<ChatMessageResponse> sink = Sinks.many().multicast().onBackpressureBuffer();
        userSinkMap.put(userId, sink);

        // ping 전송 + 메시지 병합
        return Flux.merge(
                sink.asFlux().map(data -> ServerSentEvent.builder(data).build()),
                Flux.interval(Duration.ofSeconds(5)) // Ping
                        .map(seq -> ServerSentEvent.<ChatMessageResponse>builder()
                                .event("ping")
                                .build())
        ).doFinally(signal -> {
            userSinkMap.remove(userId); // 연결 종료 시 정리
        });
    }

    @Override
    public void broadcast(ChatMessageResponse message) {
        userSinkMap.values().forEach(sink -> sink.tryEmitNext(message));
    }
}
