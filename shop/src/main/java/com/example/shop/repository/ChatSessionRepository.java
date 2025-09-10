package com.example.shop.repository;

import com.example.shop.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    
    Optional<ChatSession> findBySessionId(String sessionId);
    
    List<ChatSession> findByUserIdAndUserTypeOrderByUpdatedAtDesc(Long userId, com.example.shop.entity.ChatMessage.UserType userType);
    
    List<ChatSession> findByStatusOrderByUpdatedAtDesc(com.example.shop.entity.ChatSession.SessionStatus status);
    
    List<ChatSession> findByAssignedAgentIdOrderByUpdatedAtDesc(Long agentId);
}
