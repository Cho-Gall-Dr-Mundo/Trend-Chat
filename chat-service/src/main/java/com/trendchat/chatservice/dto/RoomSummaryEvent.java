package com.trendchat.chatservice.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RoomSummaryEvent(
        Long roomId,
        String type,
        LocalDateTime timestamp
) {
}
