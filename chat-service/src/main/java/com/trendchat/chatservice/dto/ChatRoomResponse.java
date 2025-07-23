package com.trendchat.chatservice.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
@Builder
public record ChatRoomResponse(
        Long id,
        String title,
        String description,
        LocalDateTime createdAt,
        List<ChatMessageResponse> messages) {
}
