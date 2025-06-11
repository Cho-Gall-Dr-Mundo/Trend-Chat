package com.trendchat.chatservice.service;

import com.trendchat.chatservice.dto.ChatMessageRequest;
import com.trendchat.chatservice.dto.ChatMessageResponse;
import com.trendchat.chatservice.entity.ChatMessage;

import java.util.List;

public interface ChatService {
    // DB에 메시지를 저장
    ChatMessage saveMessage(ChatMessage chatMessage);
    // DB에서 메시지 전체 조회(시간대 정렬)
    List<ChatMessageResponse> getMessageHistory(Long roomId, String currentUserEmail);
    void handleMessage(ChatMessageRequest messageRequest);
}
