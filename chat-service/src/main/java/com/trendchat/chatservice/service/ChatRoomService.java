package com.trendchat.chatservice.service;

import com.trendchat.chatservice.dto.ChatRoomResponse;
import com.trendchat.chatservice.entity.ChatRoom;
import java.util.List;
import java.util.Optional;

public interface ChatRoomService {

    boolean createChatRoom(String title, String description);

    List<ChatRoom> getAllChatRooms();

    ChatRoomResponse getChatRoomByIdResponse(Long roomId, String currentUserId);

    ChatRoom getChatRoomById(Long roomId);

    Optional<ChatRoomResponse> findResponseByTitle(String title, String currentUserId);

    ChatRoomResponse createByTitle(String title, String userId);
}
