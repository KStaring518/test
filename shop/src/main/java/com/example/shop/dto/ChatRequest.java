package com.example.shop.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ChatRequest {
    
    @NotBlank(message = "消息内容不能为空")
    private String message;
    
    @NotBlank(message = "会话ID不能为空")
    private String sessionId;
    
    private Long userId;
    
    @NotNull(message = "用户类型不能为空")
    private String userType; // USER, MERCHANT, ADMIN
    
    private String context; // 上下文信息，如当前页面、商品ID等
}
