package com.example.shop.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatResponse {
    
    private String messageId;
    private String content;
    private String sessionId;
    private Double confidence;
    private Boolean isHandledByHuman;
    private String suggestedActions; // 建议的快捷操作
    private LocalDateTime timestamp;
    private String status; // SUCCESS, LOW_CONFIDENCE, ERROR
    
    public ChatResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ChatResponse(String content, String sessionId) {
        this();
        this.content = content;
        this.sessionId = sessionId;
        this.status = "SUCCESS";
    }
}
