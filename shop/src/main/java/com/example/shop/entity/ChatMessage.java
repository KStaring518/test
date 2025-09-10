package com.example.shop.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
public class ChatMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "session_id", nullable = false)
    private String sessionId;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "user_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserType userType;
    
    @Column(name = "message_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageType messageType;
    
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "confidence")
    private Double confidence;
    
    @Column(name = "is_handled_by_human")
    private Boolean isHandledByHuman = false;
    
    public enum UserType {
        USER,       // 普通用户
        MERCHANT,   // 商家
        ADMIN       // 管理员
    }
    
    public enum MessageType {
        USER_QUESTION,      // 用户问题
        BOT_ANSWER,         // 机器人回答
        HUMAN_ANSWER,       // 人工回答
        SYSTEM_MESSAGE      // 系统消息
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
