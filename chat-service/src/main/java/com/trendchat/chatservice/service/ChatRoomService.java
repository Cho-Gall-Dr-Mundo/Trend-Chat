package com.trendchat.chatservice.service;

import com.trendchat.chatservice.entity.ChatRoom;

import java.util.List;

public interface ChatRoomService {
    ChatRoom createChatRoom(String title, String description);
    List<ChatRoom> getAllChatRooms();
    ChatRoom getChatRoomById(Long roomId);
}
