package com.trendchat.chatservice.dto;

import lombok.Builder;

@Builder
public record ChatMessageRequest(
    Long roomId,
    String sender,
    String senderNickName,
    String content
) {
}
