package com.trendchat.chatservice.service;

import com.trendchat.chatservice.dto.ChatRoomMemberDto;

import java.util.List;

public interface ChatRoomMemberService {
    void joinRoom(Long roomId, String userId, String nickname);
    List<ChatRoomMemberDto> getMembers(Long roomId);
}
