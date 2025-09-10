package com.example.shop.service;

import java.util.List;

public interface AIService {
    
    /**
     * 智能问答
     */
    String chat(String userQuestion, String context, List<String> knowledgeBase);
    
    /**
     * 计算问题与知识库的相似度
     */
    double calculateSimilarity(String question, String knowledge);
    
    /**
     * 从知识库中检索相关答案
     */
    List<String> retrieveRelevantAnswers(String question, List<String> knowledgeBase);
    
    /**
     * 生成智能回复
     */
    String generateResponse(String question, String context, List<String> relevantAnswers);
}
