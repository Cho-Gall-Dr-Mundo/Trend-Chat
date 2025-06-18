package com.trendchat.chatservice.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatRoomListResponse(
        Long id,
        String title,
        String description,
        LocalDateTime createdAt
) {}
