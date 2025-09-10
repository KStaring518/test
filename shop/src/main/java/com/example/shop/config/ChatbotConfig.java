package com.example.shop.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "chatbot")
public class ChatbotConfig {
    
    private String ollamaUrl = "http://localhost:11434";
    private String model = "qwen2.5:7b";
    private int maxTokens = 2048;
    private double temperature = 0.7;
    private String systemPrompt = "你是一个专业的零食商城客服助手，能够帮助用户解答关于商品、订单、支付、退换货等问题。请用友好、专业的语气回答用户问题。";
    
    // Getters and Setters
    public String getOllamaUrl() {
        return ollamaUrl;
    }
    
    public void setOllamaUrl(String ollamaUrl) {
        this.ollamaUrl = ollamaUrl;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public int getMaxTokens() {
        return maxTokens;
    }
    
    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }
    
    public double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
    
    public String getSystemPrompt() {
        return systemPrompt;
    }
    
    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }
}
