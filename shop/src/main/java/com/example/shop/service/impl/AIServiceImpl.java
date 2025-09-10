package com.example.shop.service.impl;

import com.example.shop.config.ChatbotConfig;
import com.example.shop.service.AIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AIServiceImpl implements AIService {
    
    @Autowired
    private ChatbotConfig chatbotConfig;
    
    private final WebClient webClient;
    
    public AIServiceImpl() {
        this.webClient = WebClient.builder().build();
    }
    
    @Override
    public String chat(String userQuestion, String context, List<String> knowledgeBase) {
        try {
            // 1. 从知识库检索相关答案
            List<String> relevantAnswers = retrieveRelevantAnswers(userQuestion, knowledgeBase);
            
            // 2. 构建增强的上下文
            String enhancedContext = buildEnhancedContext(context, relevantAnswers);
            
            // 3. 构建更智能的系统提示词
            String systemPrompt = buildSystemPrompt(context, relevantAnswers);
            
            // 4. 调用Ollama API生成回复
            String response = callOllamaAPI(userQuestion, enhancedContext, systemPrompt);
            
            // 5. 如果Ollama不可用，使用规则引擎
            if (response == null || response.trim().isEmpty()) {
                response = ruleBasedResponse(userQuestion, relevantAnswers);
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("AI chat error: ", e);
            return ruleBasedResponse(userQuestion, knowledgeBase);
        }
    }
    
    @Override
    public double calculateSimilarity(String question, String knowledge) {
        // 简单的关键词匹配相似度计算
        String[] questionWords = question.toLowerCase().split("\\s+");
        String[] knowledgeWords = knowledge.toLowerCase().split("\\s+");
        
        Set<String> questionSet = new HashSet<>(Arrays.asList(questionWords));
        Set<String> knowledgeSet = new HashSet<>(Arrays.asList(knowledgeWords));
        
        Set<String> intersection = new HashSet<>(questionSet);
        intersection.retainAll(knowledgeSet);
        
        Set<String> union = new HashSet<>(questionSet);
        union.addAll(knowledgeSet);
        
        if (union.isEmpty()) return 0.0;
        return (double) intersection.size() / union.size();
    }
    
    @Override
    public List<String> retrieveRelevantAnswers(String question, List<String> knowledgeBase) {
        if (knowledgeBase == null || knowledgeBase.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 计算相似度并排序
        List<Map.Entry<String, Double>> scoredAnswers = knowledgeBase.stream()
            .map(knowledge -> {
                double score = calculateSimilarity(question, knowledge);
                return new AbstractMap.SimpleEntry<>(knowledge, score);
            })
            .filter(entry -> entry.getValue() > 0.1) // 过滤低相似度
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(3) // 取前3个最相关的
            .collect(Collectors.toList());
        
        return scoredAnswers.stream()
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    @Override
    public String generateResponse(String question, String context, List<String> relevantAnswers) {
        if (relevantAnswers.isEmpty()) {
            return "抱歉，我没有找到相关的信息。请您换个方式提问，或者转人工客服获取帮助。";
        }
        
        // 基于相关答案生成回复
        StringBuilder response = new StringBuilder();
        response.append("根据您的问题，我为您找到以下信息：\n\n");
        
        for (int i = 0; i < relevantAnswers.size(); i++) {
            response.append(i + 1).append(". ").append(relevantAnswers.get(i)).append("\n\n");
        }
        
        response.append("如果这些信息不能完全解答您的问题，请告诉我更多细节，或者点击\"转人工客服\"获取进一步帮助。");
        
        return response.toString();
    }
    
    private String buildEnhancedContext(String context, List<String> relevantAnswers) {
        StringBuilder enhancedContext = new StringBuilder();
        
        if (context != null && !context.trim().isEmpty()) {
            enhancedContext.append("当前页面：").append(context).append("\n\n");
        }
        
        if (!relevantAnswers.isEmpty()) {
            enhancedContext.append("相关参考信息：\n");
            for (int i = 0; i < relevantAnswers.size(); i++) {
                enhancedContext.append(i + 1).append(". ").append(relevantAnswers.get(i)).append("\n");
            }
            enhancedContext.append("\n");
        }
        
        return enhancedContext.toString();
    }
    
    private String buildSystemPrompt(String context, List<String> relevantAnswers) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(chatbotConfig.getSystemPrompt());
        
        // 替换上下文占位符
        if (context != null && !context.trim().isEmpty()) {
            prompt = new StringBuilder(prompt.toString().replace("{{context}}", context));
        } else {
            prompt = new StringBuilder(prompt.toString().replace("{{context}}", "未知页面"));
        }
        
        // 如果有相关知识库信息，添加到提示词中
        if (!relevantAnswers.isEmpty()) {
            prompt.append("\n\n## 相关知识库信息\n");
            for (int i = 0; i < relevantAnswers.size(); i++) {
                prompt.append(i + 1).append(". ").append(relevantAnswers.get(i)).append("\n");
            }
            prompt.append("\n请基于以上信息回答用户问题，如果信息不足，请引导用户到相关页面查看。");
        }
        
        return prompt.toString();
    }
    
    private String callOllamaAPI(String userQuestion, String enhancedContext, String systemPrompt) {
        try {
            // 构建Ollama Chat API请求
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", chatbotConfig.getModel());
            
            // 构建消息数组
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 系统消息
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt); // 使用构建好的系统提示词
            messages.add(systemMessage);
            
            // 用户消息
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", enhancedContext + userQuestion); // 增强的上下文 + 用户问题
            messages.add(userMessage);
            
            requestBody.put("messages", messages);
            requestBody.put("stream", false);
            
            // 选项配置
            Map<String, Object> options = new HashMap<>();
            options.put("temperature", chatbotConfig.getTemperature());
            options.put("num_predict", chatbotConfig.getMaxTokens());
            requestBody.put("options", options);
            
            log.info("Calling Ollama API with request: {}", requestBody);
            
            // 调用Ollama Chat API
            String response = webClient.post()
                .uri(chatbotConfig.getOllamaUrl() + "/api/chat")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
            
            log.info("Ollama API response: {}", response);
            
            if (response != null && !response.trim().isEmpty()) {
                // 解析Ollama Chat API响应
                if (response.contains("\"message\":")) {
                    // 查找message对象中的content字段
                    int messageStart = response.indexOf("\"message\":");
                    if (messageStart != -1) {
                        int contentStart = response.indexOf("\"content\":", messageStart);
                        if (contentStart != -1) {
                            int contentValueStart = response.indexOf("\"", contentStart + 10) + 1;
                            int contentValueEnd = response.indexOf("\"", contentValueStart);
                            if (contentValueStart != -1 && contentValueEnd != -1) {
                                String responseContent = response.substring(contentValueStart, contentValueEnd);
                                if (responseContent != null && !responseContent.trim().isEmpty()) {
                                    return responseContent.trim();
                                }
                            }
                        }
                    }
                }
                
                // 如果上面的解析失败，尝试旧的generate API格式
                if (response.contains("\"response\":")) {
                    String[] parts = response.split("\"response\":");
                    if (parts.length > 1) {
                        String responseContent = parts[1].split("\"")[1];
                        if (responseContent != null && !responseContent.trim().isEmpty()) {
                            return responseContent.trim();
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            log.warn("Ollama API call failed: ", e);
        }
        
        return null; // Ollama不可用时返回null
    }
    
    private String ruleBasedResponse(String question, List<String> knowledgeBase) {
        String lowerQuestion = question.toLowerCase();
        
        // 基于规则的智能回复
        if (lowerQuestion.contains("订单") || lowerQuestion.contains("查询")) {
            return "您可以通过\"我的订单\"页面查看订单状态。如果有具体订单号，我可以帮您查询详细信息。";
        } else if (lowerQuestion.contains("退换货") || lowerQuestion.contains("退款")) {
            return "我们支持7天无理由退换货。您可以在订单详情页申请退款，客服会在24小时内处理。";
        } else if (lowerQuestion.contains("配送") || lowerQuestion.contains("快递")) {
            return "我们使用顺丰、圆通等知名快递公司，一般1-3天送达。您可以在订单详情页查看物流信息。";
        } else if (lowerQuestion.contains("支付") || lowerQuestion.contains("支付宝")) {
            return "我们支持支付宝、微信支付等多种支付方式。支付成功后订单会立即确认。";
        } else if (lowerQuestion.contains("保质期") || lowerQuestion.contains("过期")) {
            return "我们的零食商品保质期一般在6-12个月，具体保质期请查看商品详情页或包装上的标识。";
        } else if (lowerQuestion.contains("客服") || lowerQuestion.contains("人工")) {
            return "您可以通过智能客服转人工，或拨打客服热线400-123-4567，客服工作时间为9:00-18:00。";
        } else if (lowerQuestion.contains("发货") || lowerQuestion.contains("多久")) {
            return "正常情况下，我们会在24小时内发货。如遇节假日或特殊情况，可能会延迟1-2天。";
        } else {
            // 尝试从知识库中找到相关答案
            List<String> relevantAnswers = retrieveRelevantAnswers(question, knowledgeBase);
            if (!relevantAnswers.isEmpty()) {
                return generateResponse(question, "", relevantAnswers);
            }
            
            return "感谢您的咨询！我是零食商城的智能客服助手，可以帮您解答关于订单、商品、支付、退换货等问题。请问还有什么可以帮助您的吗？";
        }
    }
}
