package com.trendchat.chatservice.service;

import com.trendchat.chatservice.entity.ChatMessage;
import com.trendchat.chatservice.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatServiceImpl implements ChatService{
    private final ChatMessageRepository repository;

    @Override
    @Transactional
    public ChatMessage saveMessage(ChatMessage message){
        return repository.save(message);
    }

    @Override
    public List<ChatMessage> getMessageHistory(String roomId){
        return repository.findByRoomIdOrderByTimestampAsc(roomId);
    }
}
