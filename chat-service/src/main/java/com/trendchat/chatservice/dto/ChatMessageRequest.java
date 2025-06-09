package com.trendchat.chatservice.dto;

public record ChatMessageRequest(
    Long roomId,
    String sender,
    String content
) {
}
