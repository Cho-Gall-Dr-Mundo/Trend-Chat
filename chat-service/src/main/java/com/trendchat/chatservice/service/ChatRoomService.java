package com.trendchat.chatservice.service;

import com.trendchat.chatservice.entity.ChatRoom;
import java.util.List;

public interface ChatRoomService {

    boolean createChatRoom(String title, String description);

    List<ChatRoom> getAllChatRooms();

    ChatRoom getChatRoomById(Long roomId);
    ChatRoom getOrCreateByTitle(String title);
}
