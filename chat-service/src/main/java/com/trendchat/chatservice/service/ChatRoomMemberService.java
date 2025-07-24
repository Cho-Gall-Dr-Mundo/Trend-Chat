package com.trendchat.chatservice.service;

import com.trendchat.chatservice.dto.ChatRoomMemberDto;
import com.trendchat.trendchatcommon.enums.UserRole;

import java.util.List;

public interface ChatRoomMemberService {
    void joinRoom(Long roomId, String userId, String nickname, UserRole userRole);
    List<ChatRoomMemberDto> getMembers(Long roomId);
}
