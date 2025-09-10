package com.example.shop.controller;

import com.example.shop.dto.ChatRequest;
import com.example.shop.dto.ChatResponse;
import com.example.shop.service.ChatService;
import com.example.shop.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Arrays;
import com.example.shop.service.ChatbotProductService;
import com.example.shop.entity.Product;
import com.example.shop.entity.Category;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@PreAuthorize("permitAll()")
public class ChatController {
    
    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatbotProductService chatbotProductService;
    
    /**
     * 发送聊天消息
     */
    @PostMapping("/send")
    public Result<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request) {
        try {
            ChatResponse response = chatService.chat(request);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error("发送消息失败：" + e.getMessage());
        }
    }
    
    /**
     * 创建新的聊天会话
     */
    @PostMapping("/session")
    public Result<String> createSession(@RequestParam Long userId, @RequestParam String userType) {
        try {
            String sessionId = chatService.createSession(userId, userType);
            return Result.success(sessionId);
        } catch (Exception e) {
            return Result.error("创建会话失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取聊天历史
     */
    @GetMapping("/history/{sessionId}")
    public Result<List<ChatResponse>> getChatHistory(@PathVariable String sessionId) {
        try {
            List<ChatResponse> history = chatService.getChatHistory(sessionId);
            return Result.success(history);
        } catch (Exception e) {
            return Result.error("获取聊天历史失败：" + e.getMessage());
        }
    }
    
    /**
     * 关闭聊天会话
     */
    @DeleteMapping("/session/{sessionId}")
    public Result<Void> closeSession(@PathVariable String sessionId) {
        try {
            chatService.closeSession(sessionId);
            return Result.success();
        } catch (Exception e) {
            return Result.error("关闭会话失败：" + e.getMessage());
        }
    }
    
    /**
     * 转人工客服
     */
    @PostMapping("/transfer/{sessionId}")
    public Result<ChatResponse> transferToHuman(@PathVariable String sessionId) {
        try {
            ChatResponse response = chatService.transferToHuman(sessionId);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error("转人工失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取快捷问题模板
     */
    @GetMapping("/quick-questions")
    public Result<List<String>> getQuickQuestions() {
        List<String> questions = Arrays.asList(
            "如何查询订单状态？",
            "退换货政策是什么？",
            "配送时间需要多久？",
            "支持哪些支付方式？",
            "如何联系客服？"
        );
        return Result.success(questions);
    }

    /**
     * 智能客服商品查询API
     */
    @PostMapping("/products/search")
    @PreAuthorize("permitAll()")
    public Result<List<Product>> searchProducts(@RequestBody Map<String, Object> request) {
        String keyword = (String) request.get("keyword");
        String type = (String) request.get("type");
        Integer limit = (Integer) request.getOrDefault("limit", 5);
        
        List<Product> products;
        if (keyword != null && !keyword.trim().isEmpty()) {
            products = chatbotProductService.searchProductsByKeyword(keyword, limit);
        } else if (type != null && !type.trim().isEmpty()) {
            products = chatbotProductService.getRecommendationsByType(type, limit);
        } else {
            products = chatbotProductService.getPopularProducts(limit);
        }
        
        return Result.success(products);
    }

    /**
     * 智能客服商品存在性检查API
     */
    @PostMapping("/products/check")
    @PreAuthorize("permitAll()")
    public Result<Map<String, Object>> checkProduct(@RequestBody Map<String, Object> request) {
        String productName = (String) request.get("productName");
        
        if (productName == null || productName.trim().isEmpty()) {
            return Result.error("商品名称不能为空");
        }
        
        boolean exists = chatbotProductService.productExists(productName);
        String stockInfo = chatbotProductService.getProductStockInfo(productName);
        
        Map<String, Object> result = new HashMap<>();
        result.put("exists", exists);
        result.put("stockInfo", stockInfo);
        
        return Result.success(result);
    }

    /**
     * 智能客服获取商品分类API
     */
    @GetMapping("/products/categories")
    @PreAuthorize("permitAll()")
    public Result<List<Category>> getCategories() {
        List<Category> categories = chatbotProductService.getAllEnabledCategories();
        return Result.success(categories);
    }
    
    /**
     * 测试接口 - 验证控制器是否被正确加载
     */
    @GetMapping("/test")
    public String test() {
        return "ChatController is working!";
    }

    /**
     * 测试智能客服数据库查询功能
     */
    @GetMapping("/test/database")
    @PreAuthorize("permitAll()")
    public Result<Map<String, Object>> testDatabaseQuery() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 测试热门商品查询
            List<Product> popularProducts = chatbotProductService.getPopularProducts(5);
            result.put("popularProducts", popularProducts);
            
            // 测试分类查询
            List<Category> categories = chatbotProductService.getAllEnabledCategories();
            result.put("categories", categories);
            
            // 测试关键词搜索
            List<Product> searchResults = chatbotProductService.searchProductsByKeyword("糖", 3);
            result.put("searchResults", searchResults);
            
            // 测试商品存在性检查
            boolean hasChocolate = chatbotProductService.productExists("巧克力");
            result.put("hasChocolate", hasChocolate);
            
            // 测试商品库存信息
            String stockInfo = chatbotProductService.getProductStockInfo("巧克力");
            result.put("chocolateStockInfo", stockInfo);
            
            result.put("status", "success");
            result.put("message", "数据库查询测试成功");
            
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "数据库查询测试失败：" + e.getMessage());
            result.put("error", e.toString());
        }
        
        return Result.success(result);
    }
}
