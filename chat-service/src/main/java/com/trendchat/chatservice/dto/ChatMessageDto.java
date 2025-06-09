package com.trendchat.chatservice.dto;

import java.io.Serializable;

public record ChatMessageDto(
        Long roomId,
        String sender,
        String content
) implements Serializable {}
