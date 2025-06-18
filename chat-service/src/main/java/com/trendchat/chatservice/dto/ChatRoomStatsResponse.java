package com.trendchat.chatservice.dto;

import lombok.Builder;

@Builder
public record ChatRoomStatsResponse(
        int participants,
        int messageCount
) {
}
