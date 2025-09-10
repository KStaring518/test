package com.example.shop.repository;

import com.example.shop.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(String sessionId);
    
    List<ChatMessage> findByUserIdAndUserTypeOrderByCreatedAtDesc(Long userId, ChatMessage.UserType userType);
    
    List<ChatMessage> findBySessionIdAndMessageTypeOrderByCreatedAtAsc(String sessionId, ChatMessage.MessageType messageType);
}
