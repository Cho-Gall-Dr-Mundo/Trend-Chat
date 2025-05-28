package com.trendchat.chatservice.dto;

public record ChatMessageRequest(
    String roomId,
    String sender,
    String content
) {
}
