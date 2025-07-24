package com.trendchat.chatservice.service;

import com.trendchat.chatservice.dto.ChatRoomMemberDto;
import com.trendchat.chatservice.entity.ChatRoom;
import com.trendchat.chatservice.entity.ChatRoomMember;
import com.trendchat.chatservice.repository.ChatRoomMemberRepository;
import com.trendchat.trendchatcommon.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomMemberServiceImpl implements ChatRoomMemberService{

    private final ChatRoomService chatRoomService;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    @Override
    public void joinRoom(Long roomId, String userId, String nickname, UserRole userRole) {
        if(chatRoomMemberRepository.existsByChatRoomIdAndUserId(roomId, userId)) return;
        validateSubscription(userRole, userId);
        ChatRoom room = chatRoomService.getChatRoomById(roomId);
        ChatRoomMember member = ChatRoomMember.builder()
                .chatRoom(room)
                .userId(userId)
                .nicknameSnapshot(nickname)
                .joinedAt(LocalDateTime.now())
                .build();

        chatRoomMemberRepository.save(member);
    }

    @Override
    public List<ChatRoomMemberDto> getMembers(Long roomId) {
        return chatRoomMemberRepository.findByChatRoomId(roomId).stream()
                .map(m -> new ChatRoomMemberDto(m.getUserId(), m.getNicknameSnapshot()))
                .toList();
    }

    private void validateSubscription(UserRole role, String userId) {
        if(role == UserRole.ROLE_PREMIUM) return;

        long count = chatRoomMemberRepository.countUserSubscriptions(userId);
        if(count >= 5) throw new IllegalStateException("최대 5개의 채팅방에 가입할 수 있습니다.");
    }
}
