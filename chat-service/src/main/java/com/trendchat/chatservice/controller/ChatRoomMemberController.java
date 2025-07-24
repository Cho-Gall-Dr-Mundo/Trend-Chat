package com.trendchat.chatservice.controller;

import com.trendchat.chatservice.dto.ChatRoomMemberDto;
import com.trendchat.chatservice.service.ChatRoomMemberService;
import com.trendchat.trendchatcommon.auth.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rooms")
public class ChatRoomMemberController {

    private final ChatRoomMemberService chatRoomMemberService;

    @GetMapping("/{roomId}/members")
    public List<ChatRoomMemberDto> getMembers(@PathVariable Long roomId) {
        return chatRoomMemberService.getMembers(roomId);
    }

    @PostMapping("/{roomId}/members")
    public void joinRoom(@PathVariable Long roomId, @AuthenticationPrincipal AuthUser user) {
        chatRoomMemberService.joinRoom(roomId, user.getUserId(), user.getNickname(), user.getUserRole());
    }
}
