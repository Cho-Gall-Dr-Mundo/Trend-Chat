package com.trendchat.chatservice.service;

import com.trendchat.chatservice.dto.ChatRoomResponse;
import com.trendchat.chatservice.entity.ChatRoom;
import java.util.List;

public interface ChatRoomService {

    boolean createChatRoom(String title, String description);

    List<ChatRoom> getAllChatRooms();

    ChatRoomResponse getChatRoomByIdResponse(Long roomId, String currentUserId);

    ChatRoom getChatRoomById(Long roomId);

    ChatRoomResponse getOrCreateByTitle(String title, String currentUserId);
}
