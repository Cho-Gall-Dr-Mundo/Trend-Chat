package com.trendchat.chatservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trendchat.chatservice.dto.RoomSummaryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {
    private final SummarySseService summarySseService;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try{
            String json = new String(message.getBody(), StandardCharsets.UTF_8);
            RoomSummaryEvent event = objectMapper.readValue(json, RoomSummaryEvent.class);
            summarySseService.pushToSubscribers(event);
        }catch (Exception e){
            log.error("Redis Subscribe 실패", e);
        }
    }
}
