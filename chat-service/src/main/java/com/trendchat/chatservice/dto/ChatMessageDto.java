package com.trendchat.chatservice.dto;

import java.io.Serializable;

public record ChatMessageDto(
        Long roomId,
        String senderId,
        String senderNickname,
        String content
) implements Serializable {}
