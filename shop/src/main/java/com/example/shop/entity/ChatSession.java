package com.example.shop.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_sessions")
@Data
public class ChatSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "session_id", nullable = false, unique = true)
    private String sessionId;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "user_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ChatMessage.UserType userType;
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private SessionStatus status;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "message_count", nullable = false)
    private Integer messageCount = 0;
    
    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;
    
    @Column(name = "assigned_agent_id")
    private Long assignedAgentId;
    
    public enum SessionStatus {
        ACTIVE,         // 活跃
        WAITING,        // 等待人工
        CLOSED,         // 已关闭
        TRANSFERRED     // 已转人工
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
