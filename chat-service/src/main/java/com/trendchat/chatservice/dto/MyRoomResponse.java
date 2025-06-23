package com.trendchat.chatservice.dto;

public record MyRoomResponse (
    Long id,
    String title,
    Long memberCount
){}
