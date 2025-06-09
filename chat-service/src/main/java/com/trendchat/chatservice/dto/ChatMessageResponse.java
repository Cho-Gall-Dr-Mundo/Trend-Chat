package com.trendchat.chatservice.dto;

import java.time.LocalDateTime;

public record ChatMessageResponse(
    Long id,
    String sender,
    String content,
    LocalDateTime timestamp,
    boolean isMine
) {}
