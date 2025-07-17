package com.trendchat.chatservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trendchat.chatservice.dto.RoomSummaryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisPublisher {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public void publishRoomMessage(RoomSummaryEvent event){
        try{
            String topic = "room." + event.roomId();
            String json = objectMapper.writeValueAsString(event);
            redisTemplate.convertAndSend(topic, json);
        }catch (Exception e){
            log.error("Redis Publish 실패", e);
        }
    }
}
