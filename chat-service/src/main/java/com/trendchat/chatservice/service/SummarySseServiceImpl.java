package com.trendchat.chatservice.service;


import com.trendchat.chatservice.dto.RoomSummaryEvent;
import com.trendchat.chatservice.repository.ChatRoomMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SummarySseServiceImpl implements SummarySseService {

    private final Map<String, Sinks.Many<ServerSentEvent<RoomSummaryEvent>>> sinkMap = new ConcurrentHashMap<>();
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    @Override
    public Flux<ServerSentEvent<RoomSummaryEvent>> subscribe(String userId) {

        Sinks.Many<ServerSentEvent<RoomSummaryEvent>> sink = Sinks.many().multicast().onBackpressureBuffer();
        sinkMap.put(userId, sink);

        return sink.asFlux()
                .doOnCancel(() -> sinkMap.remove(userId))
                .doOnTerminate(() -> sinkMap.remove(userId));
    }

    @Override
    public void pushToSubscribers(RoomSummaryEvent event) {
        List<String> participants = chatRoomMemberRepository.findUserIdsByRoomId(event.roomId());

        for (String userId : participants) {
            Sinks.Many<ServerSentEvent<RoomSummaryEvent>> sink = sinkMap.get(userId);
            if (sink != null) {
                sink.tryEmitNext(ServerSentEvent.builder(event).build());
            }
        }
    }
}
