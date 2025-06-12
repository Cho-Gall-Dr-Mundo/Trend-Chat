package com.trendchat.chatservice.dto;

import lombok.Builder;

import java.time.LocalDateTime;
@Builder
public record ChatMessageResponse(
    Long id,
    Long roomId,
    String senderNickname,
    String content,
    LocalDateTime timestamp,
    boolean isMine
) {}
