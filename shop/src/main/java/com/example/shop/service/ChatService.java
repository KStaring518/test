package com.example.shop.service;

import com.example.shop.dto.ChatRequest;
import com.example.shop.dto.ChatResponse;
import reactor.core.publisher.Flux;

public interface ChatService {
    
    /**
     * 发送聊天消息并获取回复
     */
    ChatResponse chat(ChatRequest request);
    
    /**
     * 流式聊天（用于实时显示）
     */
    Flux<String> chatStream(ChatRequest request);
    
    /**
     * 创建新的聊天会话
     */
    String createSession(Long userId, String userType);
    
    /**
     * 获取聊天历史
     */
    java.util.List<ChatResponse> getChatHistory(String sessionId);
    
    /**
     * 关闭聊天会话
     */
    void closeSession(String sessionId);
    
    /**
     * 转人工客服
     */
    ChatResponse transferToHuman(String sessionId);
}
