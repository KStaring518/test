package com.example.shop.service.impl;

import com.example.shop.config.ChatbotConfig;
import com.example.shop.dto.ChatRequest;
import com.example.shop.dto.ChatResponse;
import com.example.shop.entity.ChatMessage;
import com.example.shop.entity.ChatSession;
import com.example.shop.entity.KnowledgeBase;
import com.example.shop.entity.Order;
import com.example.shop.repository.ChatMessageRepository;
import com.example.shop.repository.ChatSessionRepository;
import com.example.shop.repository.KnowledgeBaseRepository;
import com.example.shop.repository.OrderRepository;
import com.example.shop.service.ChatService;
import com.example.shop.service.ChatbotProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.web.reactive.function.client.WebClient;
import com.example.shop.entity.Product;
import com.example.shop.entity.Category;
import com.example.shop.entity.Merchant;
import com.example.shop.repository.MerchantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.example.shop.repository.ProductRepository;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {
    
    @Autowired
    private ChatbotConfig chatbotConfig;
    
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    @Autowired
    private ChatSessionRepository chatSessionRepository;
    
    @Autowired
    private KnowledgeBaseRepository knowledgeBaseRepository;
    
    @Autowired
    private ChatbotProductService chatbotProductService;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private WebClient webClient;
    
    @Autowired
    private MerchantRepository merchantRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    // 内存缓存，存储会话上下文
    private final Map<String, List<Map<String, String>>> sessionContexts = new ConcurrentHashMap<>();
    
    @Override
    public ChatResponse chat(ChatRequest request) {
        try {
            // 1. 保存用户消息
            saveUserMessage(request);
            
            // 2. 从知识库检索相关信息
            List<String> knowledgeBase = retrieveKnowledgeBase(request.getMessage());
            
            // 3. 查询数据库获取真实信息
            String databaseInfo = queryDatabaseForRealInfo(request.getMessage());
            
            // 4. 构建上下文
            String context = buildContext(request);
            
            // 5. 调用AI服务（集成知识库和数据库信息）
            String botResponse = callOllamaAPI(request.getMessage(), context, knowledgeBase, databaseInfo);
            
            // 6. 计算置信度
            double confidence = calculateConfidence(request.getMessage(), botResponse, knowledgeBase, databaseInfo);
            
            // 7. 保存机器人回复
            ChatMessage botMessage = saveBotMessage(request.getSessionId(), botResponse, confidence);
            
            // 8. 更新会话状态
            updateSessionStatus(request.getSessionId(), confidence);
            
            // 9. 构建响应
            ChatResponse response = new ChatResponse(botResponse, request.getSessionId());
            response.setMessageId(botMessage.getId().toString());
            response.setConfidence(confidence);
            response.setIsHandledByHuman(false);
            
            if (confidence < 0.6) {
                response.setStatus("LOW_CONFIDENCE");
                response.setSuggestedActions("转人工客服");
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("Chat error: ", e);
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setStatus("ERROR");
            errorResponse.setContent("抱歉，我遇到了一些问题，请稍后再试或转人工客服。");
            return errorResponse;
        }
    }
    
    @Override
    public Flux<String> chatStream(ChatRequest request) {
        return Flux.just("正在思考中...", "正在生成回答...", "回答完成");
    }
    
    @Override
    public String createSession(Long userId, String userType) {
        String sessionId = UUID.randomUUID().toString();
        
        ChatSession session = new ChatSession();
        session.setSessionId(sessionId);
        session.setUserId(userId);
        session.setUserType(ChatMessage.UserType.valueOf(userType.toUpperCase()));
        session.setStatus(ChatSession.SessionStatus.ACTIVE);
        session.setMessageCount(0);
        
        chatSessionRepository.save(session);
        sessionContexts.put(sessionId, new ArrayList<>());
        
        log.info("Created new chat session: {} for user: {} type: {}", sessionId, userId, userType);
        return sessionId;
    }
    
    @Override
    public List<ChatResponse> getChatHistory(String sessionId) {
        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        List<ChatResponse> responses = new ArrayList<>();
        
        for (ChatMessage message : messages) {
            ChatResponse response = new ChatResponse();
            response.setMessageId(message.getId().toString());
            response.setContent(message.getContent());
            response.setSessionId(sessionId);
            response.setTimestamp(message.getCreatedAt());
            response.setIsHandledByHuman(message.getIsHandledByHuman());
            responses.add(response);
        }
        
        return responses;
    }
    
    @Override
    public void closeSession(String sessionId) {
        java.util.Optional<ChatSession> sessionOpt = chatSessionRepository.findBySessionId(sessionId);
        if (sessionOpt.isPresent()) {
            ChatSession session = sessionOpt.get();
            session.setStatus(ChatSession.SessionStatus.CLOSED);
            chatSessionRepository.save(session);
            sessionContexts.remove(sessionId);
        }
    }
    
    @Override
    public ChatResponse transferToHuman(String sessionId) {
        java.util.Optional<ChatSession> sessionOpt = chatSessionRepository.findBySessionId(sessionId);
        if (sessionOpt.isPresent()) {
            ChatSession session = sessionOpt.get();
            session.setStatus(ChatSession.SessionStatus.WAITING);
            chatSessionRepository.save(session);
        }
        
        ChatResponse response = new ChatResponse();
        response.setContent("正在为您转接人工客服，请稍候...");
        response.setSessionId(sessionId);
        response.setStatus("TRANSFERRED");
        return response;
    }
    
    private ChatMessage saveUserMessage(ChatRequest request) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(request.getSessionId());
        message.setUserId(request.getUserId());
        message.setUserType(ChatMessage.UserType.valueOf(request.getUserType().toUpperCase()));
        message.setMessageType(ChatMessage.MessageType.USER_QUESTION);
        message.setContent(request.getMessage());
        
        return chatMessageRepository.save(message);
    }
    
    private ChatMessage saveBotMessage(String sessionId, String content, double confidence) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setMessageType(ChatMessage.MessageType.BOT_ANSWER);
        message.setContent(content);
        message.setConfidence(confidence);
        
        // 从会话中获取用户信息
        Optional<ChatSession> sessionOpt = chatSessionRepository.findBySessionId(sessionId);
        if (sessionOpt.isPresent()) {
            ChatSession session = sessionOpt.get();
            message.setUserId(session.getUserId());
            message.setUserType(session.getUserType());
        } else {
            // 如果没有找到会话，设置默认值
            message.setUserId(0L);
            message.setUserType(ChatMessage.UserType.USER);
        }
        
        return chatMessageRepository.save(message);
    }
    
    private String buildContext(ChatRequest request) {
        StringBuilder context = new StringBuilder();
        context.append(chatbotConfig.getSystemPrompt()).append("\n\n");
        
        if (request.getContext() != null) {
            context.append("当前页面信息：").append(request.getContext()).append("\n\n");
        }
        
        return context.toString();
    }
    
    /**
     * 从知识库检索相关信息
     */
    private List<String> retrieveKnowledgeBase(String question) {
        try {
            List<KnowledgeBase> knowledgeList = knowledgeBaseRepository.searchByKeyword(question);
            return knowledgeList.stream()
                .map(kb -> String.format("问题：%s\n答案：%s", kb.getQuestion(), kb.getAnswer()))
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to retrieve knowledge base: ", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 查询数据库获取真实信息（供智能客服使用）
     */
    private String queryDatabaseForRealInfo(String message) {
        StringBuilder dbInfo = new StringBuilder();
        String lowerMessage = message.toLowerCase();
        
        try {
            // 优先：无论用户是否包含“订单”等关键词，先尝试提取订单号
            String maybeOrderNo = extractOrderNumber(message);
            if (maybeOrderNo != null) {
                dbInfo.append("## 订单查询信息\n");
                java.util.Optional<Order> orderOpt = orderRepository.findByOrderNo(maybeOrderNo);
                if (orderOpt.isPresent()) {
                    Order order = orderOpt.get();
                    dbInfo.append("订单号：").append(order.getOrderNo()).append("\n");
                    dbInfo.append("订单状态：").append(order.getStatus()).append("\n");
                    dbInfo.append("创建时间：").append(order.getCreatedAt()).append("\n");
                    dbInfo.append("总金额：").append(order.getTotalAmount()).append("元\n");
                    if (order.getStatus() == Order.OrderStatus.UNPAID) {
                        dbInfo.append("当前为未支付状态：请在订单详情完成支付\n");
                    } else if (order.getStatus() == Order.OrderStatus.PAID) {
                        dbInfo.append("当前为已支付状态：商家将在24小时内发货\n");
                    } else if (order.getStatus() == Order.OrderStatus.SHIPPED) {
                        dbInfo.append("当前为已发货状态：可在物流页查看进度\n");
                    } else if (order.getStatus() == Order.OrderStatus.FINISHED) {
                        dbInfo.append("当前为已完成状态：包裹已送达并签收\n");
                    } else if (order.getStatus() == Order.OrderStatus.CLOSED) {
                        dbInfo.append("当前为已关闭状态：如需购买请重新下单\n");
                    }
                    // 物流
                    if (order.getShipment() != null) {
                        dbInfo.append("\n## 物流信息\n");
                        dbInfo.append("物流公司：").append(order.getShipment().getCarrier()).append("\n");
                        dbInfo.append("物流单号：").append(order.getShipment().getTrackingNo()).append("\n");
                        dbInfo.append("发货时间：").append(order.getShipment().getShippedAt()).append("\n");
                        dbInfo.append("物流状态：").append(order.getShipment().getStatus()).append("\n");
                    }
                } else {
                    dbInfo.append("注意：未找到订单号'").append(maybeOrderNo).append("'的订单\n");
                }
            }
            
            // 1. 查询订单相关信息（关键字触发，保留原有逻辑）
            if (lowerMessage.contains("订单") || lowerMessage.contains("问题") || 
                lowerMessage.contains("没收到") || lowerMessage.contains("发货") ||
                lowerMessage.contains("签收") || lowerMessage.contains("送达")) {
                
                if (dbInfo.indexOf("订单查询信息") == -1) {
                    dbInfo.append("## 订单查询信息\n");
                }
                
                // 尝试从消息中提取订单号
                String orderNumber = maybeOrderNo != null ? maybeOrderNo : extractOrderNumber(message);
                if (orderNumber != null) {
                    // 查询具体订单
                    Optional<Order> orderOpt = orderRepository.findByOrderNo(orderNumber);
                    if (orderOpt.isPresent()) {
                        Order order = orderOpt.get();
                        dbInfo.append("订单号：").append(order.getOrderNo()).append("\n");
                        dbInfo.append("订单状态：").append(order.getStatus()).append("\n");
                        dbInfo.append("创建时间：").append(order.getCreatedAt()).append("\n");
                        dbInfo.append("总金额：").append(order.getTotalAmount()).append("元\n");
                        
                        // 根据实际订单状态提供具体建议
                        if (order.getStatus() == Order.OrderStatus.UNPAID) {
                            dbInfo.append("订单状态：待付款，请及时完成支付\n");
                        } else if (order.getStatus() == Order.OrderStatus.PAID) {
                            dbInfo.append("订单状态：已付款，商家将在24小时内发货\n");
                        } else if (order.getStatus() == Order.OrderStatus.SHIPPED) {
                            dbInfo.append("订单状态：已发货，请关注物流信息\n");
                        } else if (order.getStatus() == Order.OrderStatus.FINISHED) {
                            dbInfo.append("订单状态：已完成，包裹已送达并签收\n");
                            dbInfo.append("如果未收到商品，可能原因：\n");
                            dbInfo.append("1. 快递员已投递到指定地址\n");
                            dbInfo.append("2. 家人或同事代收\n");
                            dbInfo.append("3. 快递柜或代收点\n");
                            dbInfo.append("建议：联系快递公司确认具体投递位置\n");
                        } else if (order.getStatus() == Order.OrderStatus.CLOSED) {
                            dbInfo.append("订单状态：已关闭，如需购买请重新下单\n");
                        }
                        
                        // 查询物流信息（如果有）
                        if (order.getShipment() != null) {
                            dbInfo.append("\n## 物流信息\n");
                            dbInfo.append("物流公司：").append(order.getShipment().getCarrier()).append("\n");
                            dbInfo.append("物流单号：").append(order.getShipment().getTrackingNo()).append("\n");
                            dbInfo.append("发货时间：").append(order.getShipment().getShippedAt()).append("\n");
                            dbInfo.append("物流状态：").append(order.getShipment().getStatus()).append("\n");
                        }
                        
                    } else {
                        dbInfo.append("注意：未找到订单号'").append(orderNumber).append("'的订单\n");
                        dbInfo.append("请确认订单号是否正确，或联系客服查询\n");
                    }
                } else {
                    dbInfo.append("提示：如需查询具体订单，请提供订单号\n");
                    dbInfo.append("订单号格式：以字母'O'开头的18位字符\n");
                }
                
                // 如果用户说没收到商品，提供具体解决方案
                if (lowerMessage.contains("没收到") || lowerMessage.contains("未收到")) {
                    dbInfo.append("\n## 收货问题处理\n");
                    dbInfo.append("如果订单显示已完成但未收到商品：\n");
                    dbInfo.append("1. 检查快递柜、代收点、门卫处\n");
                    dbInfo.append("2. 联系家人、同事确认是否代收\n");
                    dbInfo.append("3. 联系快递公司确认具体投递位置\n");
                    dbInfo.append("4. 如确实未收到，我们将协助处理退款或补发\n");
                    dbInfo.append("5. 建议保留相关证据（照片、视频等）\n");
                }
            }
            
                        // 2. 查询商品相关信息 - 大幅扩展查询范围
             if (lowerMessage.contains("推荐") || lowerMessage.contains("零食") || 
                 lowerMessage.contains("商品") || lowerMessage.contains("有") ||
                 lowerMessage.contains("吗") || lowerMessage.contains("么") ||
                 lowerMessage.contains("什么") || lowerMessage.contains("哪些")) {
                 
                 dbInfo.append("\n## 数据库真实商品信息\n");
                 log.info("触发商品查询逻辑，消息: {}", message);
                
                                 // 2.0 商家商品查询（新增功能）
                 // 检查是否包含商家相关关键词，或者包含"这个商家"、"都有什么"等模式
                 // 放宽触发条件，只要包含"有什么"、"都有什么"等就触发
                 log.info("检查商家查询触发条件...");
                 log.info("lowerMessage.contains('商家'): {}", lowerMessage.contains("商家"));
                 log.info("lowerMessage.contains('店铺'): {}", lowerMessage.contains("店铺"));
                 log.info("lowerMessage.contains('店'): {}", lowerMessage.contains("店"));
                 log.info("lowerMessage.contains('商'): {}", lowerMessage.contains("商"));
                 log.info("message.contains('这个商家'): {}", message.contains("这个商家"));
                 log.info("message.contains('都有什么'): {}", message.contains("都有什么"));
                 log.info("message.contains('有什么商品'): {}", message.contains("有什么商品"));
                 log.info("message.contains('有什么'): {}", message.contains("有什么"));
                 
                 if (lowerMessage.contains("商家") || lowerMessage.contains("店铺") || 
                     lowerMessage.contains("店") || lowerMessage.contains("商") ||
                     message.contains("这个商家") || message.contains("都有什么") ||
                     message.contains("有什么商品") || message.contains("有什么") ||
                     message.contains("都有什么")) {
                     
                     log.info("✅ 触发商家查询逻辑！");
                     
                     // 提取可能的商家名称
                     String[] possibleMerchants = extractPossibleMerchantNames(message);
                     
                     // 添加调试信息
                     log.info("商家查询 - 原始消息: {}", message);
                     log.info("商家查询 - 提取的商家名称: {}", Arrays.toString(possibleMerchants));
                     
                     if (possibleMerchants.length > 0) {
                         dbInfo.append("\n## 商家商品查询\n");
                         
                         for (String merchantName : possibleMerchants) {
                             dbInfo.append("\n查询商家：").append(merchantName).append("\n");
                             log.info("正在查询商家: {}", merchantName);
                            
                                                         try {
                                 // 查询商家信息 - 改进查询逻辑
                                 Optional<Merchant> merchantOpt = Optional.empty();
                                 
                                 // 1. 精确匹配
                                 List<Merchant> exactMatches = merchantRepository.findByStatus(Merchant.MerchantStatus.APPROVED);
                                 for (Merchant m : exactMatches) {
                                     if (m.getShopName().equals(merchantName)) {
                                         merchantOpt = Optional.of(m);
                                         break;
                                     }
                                 }
                                 
                                 // 2. 包含匹配
                                 if (!merchantOpt.isPresent()) {
                                     Page<Merchant> merchantPage = merchantRepository.findByShopNameContaining(merchantName, PageRequest.of(0, 10));
                                     merchantOpt = merchantPage.getContent().stream().findFirst();
                                 }
                                 
                                 // 3. 模糊匹配（处理中文商家名称）
                                 if (!merchantOpt.isPresent()) {
                                     for (Merchant m : exactMatches) {
                                         // 双向包含匹配
                                         if (m.getShopName().contains(merchantName) || merchantName.contains(m.getShopName())) {
                                             merchantOpt = Optional.of(m);
                                             log.info("模糊匹配成功：查询名称 '{}' 匹配商家 '{}'", merchantName, m.getShopName());
                                             break;
                                         }
                                         // 添加拼音匹配（如果需要）
                                         if (m.getShopName().toLowerCase().contains(merchantName.toLowerCase()) || 
                                             merchantName.toLowerCase().contains(m.getShopName().toLowerCase())) {
                                             merchantOpt = Optional.of(m);
                                             log.info("不区分大小写匹配成功：查询名称 '{}' 匹配商家 '{}'", merchantName, m.getShopName());
                                             break;
                                         }
                                     }
                                 }
                                 
                                 // 4. 如果还是没找到，尝试更宽松的匹配
                                 if (!merchantOpt.isPresent()) {
                                     log.info("尝试更宽松的匹配，查询名称: {}", merchantName);
                                     for (Merchant m : exactMatches) {
                                         log.info("检查商家: {} (ID: {})", m.getShopName(), m.getId());
                                         // 检查是否包含关键词
                                         if (merchantName.contains("好好吃") && m.getShopName().contains("好好吃")) {
                                             merchantOpt = Optional.of(m);
                                             log.info("关键词匹配成功：'好好吃' 匹配商家 '{}'", m.getShopName());
                                             break;
                                         }
                                         if (merchantName.contains("零食") && m.getShopName().contains("零食")) {
                                             merchantOpt = Optional.of(m);
                                             log.info("关键词匹配成功：'零食' 匹配商家 '{}'", m.getShopName());
                                             break;
                                         }
                                     }
                                 }
                                 
                                                                  if (merchantOpt.isPresent()) {
                                     Merchant merchant = merchantOpt.get();
                                     dbInfo.append("店铺名称：").append(merchant.getShopName()).append("\n");
                                     dbInfo.append("店铺描述：").append(merchant.getShopDescription()).append("\n");
                                     dbInfo.append("联系电话：").append(merchant.getContactPhone()).append("\n");
                                     dbInfo.append("商家ID：").append(merchant.getId()).append("\n");
                                     
                                     // 查询该商家的商品 - 添加调试信息
                                     log.info("查询商家ID: {} 的商品", merchant.getId());
                                     List<Product> merchantProducts = productRepository.findByMerchantId(merchant.getId(), PageRequest.of(0, Integer.MAX_VALUE)).getContent();
                                     log.info("找到商品数量: {}", merchantProducts.size());
                                     
                                     if (!merchantProducts.isEmpty()) {
                                         dbInfo.append("\n该商家在售商品（共").append(merchantProducts.size()).append("件）：\n");
                                         for (Product product : merchantProducts) {
                                             if (product.getStatus() == Product.ProductStatus.ON_SALE) {
                                                 dbInfo.append("- ").append(product.getName());
                                                 if (product.getSubtitle() != null && !product.getSubtitle().isEmpty()) {
                                                     dbInfo.append("（").append(product.getSubtitle()).append("）");
                                                 }
                                                 dbInfo.append(" - ").append(product.getPrice()).append("元/").append(product.getUnit());
                                                 dbInfo.append("，库存：").append(product.getStock());
                                                 dbInfo.append("，商品ID：").append(product.getId());
                                                 dbInfo.append("，所属商家ID：").append(product.getMerchant().getId()).append("\n");
                                                 
                                                 // 验证商品所属商家
                                                 if (!product.getMerchant().getId().equals(merchant.getId())) {
                                                     log.warn("商品 {} 所属商家ID {} 与查询商家ID {} 不匹配！", 
                                                              product.getName(), product.getMerchant().getId(), merchant.getId());
                                                 }
                                             }
                                         }
                                     } else {
                                         dbInfo.append("\n注意：该商家目前没有在售商品\n");
                                     }
                                } else {
                                    dbInfo.append("\n注意：未找到名为'").append(merchantName).append("'的商家\n");
                                                                         // 提供所有商家列表
                                     List<Merchant> allMerchants = merchantRepository.findByStatus(Merchant.MerchantStatus.APPROVED);
                                     if (!allMerchants.isEmpty()) {
                                         dbInfo.append("\n可用的商家列表：\n");
                                         for (Merchant m : allMerchants) {
                                             dbInfo.append("- ").append(m.getShopName()).append(" (ID: ").append(m.getId()).append(")\n");
                                         }
                                         
                                         // 添加调试信息
                                         log.info("所有可用商家:");
                                         for (Merchant m : allMerchants) {
                                             log.info("商家: {} (ID: {})", m.getShopName(), m.getId());
                                         }
                                     }
                                }
                            } catch (Exception e) {
                                dbInfo.append("\n查询商家信息时出现异常：").append(e.getMessage()).append("\n");
                            }
                        }
                    }
                }
                
                // 获取热门商品
                List<Product> popularProducts = chatbotProductService.getPopularProducts(5);
                if (!popularProducts.isEmpty()) {
                    dbInfo.append("热门商品：\n");
                    for (Product product : popularProducts) {
                        dbInfo.append("- ").append(product.getName())
                              .append("（").append(product.getPrice()).append("元/").append(product.getUnit()).append("）\n");
                    }
                }
                
                // 2.1 饮料类商品查询（大幅扩展）
                if (lowerMessage.contains("饮料") || lowerMessage.contains("饮品") || 
                    lowerMessage.contains("咖啡") || lowerMessage.contains("奶茶") || 
                    lowerMessage.contains("果汁") || lowerMessage.contains("可乐") ||
                    lowerMessage.contains("水") || lowerMessage.contains("茶")) {
                    
                    // 尝试多种关键词搜索
                    List<Product> drinks = new ArrayList<>();
                    String[] drinkKeywords = {"咖啡", "奶茶", "果汁", "可乐", "雪碧", "芬达", "七喜", "纯净水", "矿泉水", "绿茶", "红茶", "乌龙茶"};
                    
                    for (String keyword : drinkKeywords) {
                        List<Product> found = chatbotProductService.searchProductsByKeyword(keyword, 3);
                        drinks.addAll(found);
                    }
                    
                    // 去重
                    drinks = drinks.stream()
                        .distinct()
                        .limit(10)
                        .collect(Collectors.toList());
                    
                    if (!drinks.isEmpty()) {
                        dbInfo.append("\n饮料类商品：\n");
                        for (Product product : drinks) {
                            dbInfo.append("- ").append(product.getName())
                                  .append("（").append(product.getPrice()).append("元/").append(product.getUnit()).append("）\n");
                        }
                    } else {
                        dbInfo.append("\n注意：数据库中目前没有找到饮料类商品\n");
                    }
                }
                
                // 2.2 零食类商品查询（大幅扩展）
                if (lowerMessage.contains("零食") || lowerMessage.contains("小吃") || 
                    lowerMessage.contains("果干") || lowerMessage.contains("坚果") || 
                    lowerMessage.contains("糖果") || lowerMessage.contains("巧克力") ||
                    lowerMessage.contains("薯片") || lowerMessage.contains("饼干") ||
                    lowerMessage.contains("泡面") || lowerMessage.contains("方便面")) {
                    
                    // 尝试多种关键词搜索
                    List<Product> snacks = new ArrayList<>();
                    String[] snackKeywords = {"果干", "坚果", "开心果", "杏仁", "松子", "腰果", "夏威夷果", "糖果", "巧克力", "薯片", "饼干", "泡面", "方便面"};
                    
                    for (String keyword : snackKeywords) {
                        List<Product> found = chatbotProductService.searchProductsByKeyword(keyword, 3);
                        snacks.addAll(found);
                    }
                    
                    // 去重
                    snacks = snacks.stream()
                        .distinct()
                        .limit(10)
                        .collect(Collectors.toList());
                    
                    if (!snacks.isEmpty()) {
                        dbInfo.append("\n零食类商品：\n");
                        for (Product product : snacks) {
                            dbInfo.append("- ").append(product.getName())
                                  .append("（").append(product.getPrice()).append("元/").append(product.getUnit()).append("）\n");
                        }
                    } else {
                        dbInfo.append("\n注意：数据库中目前没有找到零食类商品\n");
                    }
                }
                
                // 2.3 通用商品查询（覆盖所有可能的商品类型）
                if (lowerMessage.contains("有") && (lowerMessage.contains("吗") || lowerMessage.contains("么"))) {
                    // 提取可能的商品名称
                    String[] possibleProducts = extractPossibleProductNames(message);
                    
                    if (possibleProducts.length > 0) {
                        dbInfo.append("\n## 商品存在性检查\n");
                        
                        for (String productName : possibleProducts) {
                            boolean exists = chatbotProductService.productExists(productName);
                            if (exists) {
                                String stockInfo = chatbotProductService.getProductStockInfo(productName);
                                dbInfo.append("\n商品'").append(productName).append("'存在：\n").append(stockInfo);
                            } else {
                                dbInfo.append("\n注意：数据库中不存在商品'").append(productName).append("'\n");
                                // 提供类似商品推荐
                                List<Product> similarProducts = chatbotProductService.searchProductsByKeyword("糖", 3);
                                if (!similarProducts.isEmpty()) {
                                    dbInfo.append("类似商品推荐：\n");
                                    for (Product product : similarProducts) {
                                        dbInfo.append("- ").append(product.getName())
                                              .append("（").append(product.getPrice()).append("元/").append(product.getUnit()).append("）\n");
                                    }
                                }
                            }
                        }
                    }
                }
                
                // 2.4 分类查询
                if (lowerMessage.contains("分类") || lowerMessage.contains("类别")) {
                    List<Category> categories = chatbotProductService.getAllEnabledCategories();
                    if (!categories.isEmpty()) {
                        dbInfo.append("\n## 商品分类信息\n");
                        for (Category category : categories) {
                            dbInfo.append("- ").append(category.getName());
                            if (category.getSortOrder() != null) {
                                dbInfo.append("（排序：").append(category.getSortOrder()).append("）");
                            }
                            dbInfo.append("\n");
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            log.warn("Failed to query database for real info: ", e);
            dbInfo.append("\n## 数据库查询状态\n");
            dbInfo.append("注意：数据库查询出现异常，无法获取最新商品信息\n");
        }
        
        return dbInfo.toString();
    }

    /**
     * 调用Ollama API（集成知识库和数据库信息）
     */
    private String callOllamaAPI(String message, String context, List<String> knowledgeBase, String databaseInfo) {
        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", chatbotConfig.getModel());
            
            // 构建消息数组
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 系统消息（包含知识库和数据库信息）
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            
            StringBuilder systemContent = new StringBuilder();
            systemContent.append(chatbotConfig.getSystemPrompt());
            
            // 替换上下文占位符
            if (context != null && !context.trim().isEmpty()) {
                systemContent = new StringBuilder(systemContent.toString().replace("{{context}}", context));
            } else {
                systemContent = new StringBuilder(systemContent.toString().replace("{{context}}", "未知页面"));
            }
            
            // 添加知识库信息（优先级较低）
            if (!knowledgeBase.isEmpty()) {
                systemContent.append("\n\n## 相关知识库信息（仅供参考）\n");
                systemContent.append("**注意：以下知识库信息仅供参考，回答时必须以数据库信息为准！**\n");
                for (int i = 0; i < knowledgeBase.size(); i++) {
                    systemContent.append(i + 1).append(". ").append(knowledgeBase.get(i)).append("\n");
                }
            }
            
            // 添加数据库真实信息（优先级最高）
            if (databaseInfo != null && !databaseInfo.trim().isEmpty()) {
                systemContent.append("\n\n## 🚨 数据库真实信息（回答必须基于此）\n");
                systemContent.append(databaseInfo);
                systemContent.append("\n\n## 🚨 重要约束（必须严格遵守）\n");
                systemContent.append("1. **所有回答必须基于以上数据库真实信息**\n");
                systemContent.append("2. **绝对禁止推荐数据库中不存在的商品**\n");
                systemContent.append("3. **如果数据库中没有某类商品，必须明确说明'没有该类商品'**\n");
                systemContent.append("4. **所有商品推荐必须来自数据库查询结果**\n");
                systemContent.append("5. **不能基于知识库或系统提示词想象商品**\n");
                systemContent.append("6. **订单问题必须基于真实订单状态回答**\n");
                systemContent.append("7. **物流信息必须基于真实物流状态**\n");
                systemContent.append("8. **如果数据库查询失败，必须明确告知用户**\n");
                systemContent.append("\n**违反以上约束的回答将被视为无效回答！**");
            } else {
                // 如果没有数据库信息，添加严格约束
                systemContent.append("\n\n## 🚨 严格约束（无数据库信息时）\n");
                systemContent.append("1. **由于无法获取数据库信息，请谨慎回答**\n");
                systemContent.append("2. **绝对禁止推荐任何商品**\n");
                systemContent.append("3. **绝对禁止提供订单状态信息**\n");
                systemContent.append("4. **建议用户转人工客服获取准确信息**\n");
                systemContent.append("5. **只能提供通用的系统功能说明**\n");
            }
            
            systemContent.append("\n\n## 回答格式要求\n");
            systemContent.append("- 如果数据库中有相关信息，请基于数据库信息回答\n");
            systemContent.append("- 如果数据库中没有相关信息，请明确说明并提供替代方案\n");
            systemContent.append("- 所有回答必须具体、可操作，避免泛泛而谈\n");
            systemContent.append("- 如果信息不足，请引导用户到相关页面或转人工客服\n");
            
            systemMessage.put("content", systemContent.toString());
            messages.add(systemMessage);
            
            // 用户消息
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", message);
            messages.add(userMessage);
            
            requestBody.put("messages", messages);
            requestBody.put("stream", false);
            
            // 选项配置
            Map<String, Object> options = new HashMap<>();
            options.put("temperature", chatbotConfig.getTemperature());
            options.put("num_predict", chatbotConfig.getMaxTokens());
            requestBody.put("options", options);
            
            log.info("Calling Ollama API with strict database constraints");
            
            // 调用Ollama Chat API
            String response = webClient.post()
                .uri(chatbotConfig.getOllamaUrl() + "/api/chat")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
            
            if (response != null && !response.trim().isEmpty()) {
                // 解析Ollama Chat API响应
                if (response.contains("\"message\":")) {
                    int messageStart = response.indexOf("\"message\":");
                    if (messageStart != -1) {
                        int contentStart = response.indexOf("\"content\":", messageStart);
                        if (contentStart != -1) {
                            int contentValueStart = response.indexOf("\"", contentStart + 10) + 1;
                            int contentValueEnd = response.indexOf("\"", contentValueStart);
                            if (contentValueStart != -1 && contentValueEnd != -1) {
                                String responseContent = response.substring(contentValueStart, contentValueEnd);
                                if (responseContent != null && !responseContent.trim().isEmpty()) {
                                    // 验证回答是否符合约束
                                    String validatedResponse = validateResponse(responseContent, databaseInfo);
                                    return validatedResponse.trim();
                                }
                            }
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            log.warn("Ollama API call failed: ", e);
        }
        
        // 如果Ollama不可用，使用规则引擎
        return ruleBasedResponse(message, knowledgeBase, databaseInfo);
    }
    
    /**
     * 验证AI回答是否符合约束
     */
    private String validateResponse(String response, String databaseInfo) {
        // 如果数据库信息为空，添加警告
        if (databaseInfo == null || databaseInfo.trim().isEmpty() || 
            databaseInfo.contains("数据库查询出现异常")) {
            return "⚠️ 由于系统暂时无法获取数据库信息，我无法提供准确的商品或订单信息。\n\n" +
                   "建议您：\n" +
                   "1. 稍后再试\n" +
                   "2. 直接查看相关页面（如商品列表、我的订单）\n" +
                   "3. 转人工客服获取准确信息\n\n" +
                   "为了确保信息准确性，我不会基于假设提供任何商品推荐或订单状态信息。";
        }
        
        // 检查是否包含想象性商品推荐
        String[] imaginaryProducts = {"咖啡", "奶茶", "果汁", "可乐", "雪碧", "芬达", "七喜", "纯净水", "矿泉水", "绿茶", "红茶", "乌龙茶"};
        for (String product : imaginaryProducts) {
            if (response.contains(product) && !databaseInfo.contains(product)) {
                log.warn("AI response contains imaginary product: {}", product);
                return "⚠️ 检测到AI回答可能包含不准确的商品信息。\n\n" +
                       "为了确保信息准确性，我建议您：\n" +
                       "1. 直接查看商品列表页面\n" +
                       "2. 使用搜索功能查找具体商品\n" +
                       "3. 转人工客服获取准确信息\n\n" +
                       "我不会推荐任何未经数据库验证的商品信息。";
            }
        }
        
        // 检查是否包含泛泛而谈的回答
        String[] genericPhrases = {"请查看", "请联系", "请等待", "请稍后", "请耐心"};
        boolean isGeneric = false;
        for (String phrase : genericPhrases) {
            if (response.contains(phrase) && response.length() < 100) {
                isGeneric = true;
                break;
            }
        }
        
        if (isGeneric) {
            log.warn("AI response is too generic");
            return "⚠️ 检测到AI回答过于泛泛而谈。\n\n" +
                   "基于数据库信息，我为您提供具体的解决方案：\n\n" +
                   databaseInfo.substring(0, Math.min(databaseInfo.length(), 500)) + "\n\n" +
                   "如果以上信息无法解决您的问题，请转人工客服获取进一步帮助。";
        }
        
        return response;
    }
    
    /**
     * 计算置信度（考虑知识库和数据库信息匹配度）
     */
    private double calculateConfidence(String question, String response, List<String> knowledgeBase, String databaseInfo) {
        double baseConfidence = 0.3; // 降低基础置信度
        
        // 如果知识库有相关信息，适度提高置信度
        if (!knowledgeBase.isEmpty()) {
            baseConfidence += 0.1;
        }
        
        // 如果数据库有真实信息，大幅提高置信度
        if (databaseInfo != null && !databaseInfo.trim().isEmpty() && 
            !databaseInfo.contains("数据库查询出现异常")) {
            baseConfidence += 0.5; // 数据库信息权重最高
            
            // 检查回答是否基于数据库信息
            if (response.contains("根据我们的数据库") || 
                response.contains("数据库查询结果") ||
                response.contains("数据库信息")) {
                baseConfidence += 0.2;
            }
        }
        
        // 如果回答包含具体信息，提高置信度
        if (response.contains("我的订单") || response.contains("支付宝") || 
            response.contains("顺丰") || response.contains("圆通") ||
            response.contains("7天") || response.contains("24小时")) {
            baseConfidence += 0.1;
        }
        
        // 如果回答包含警告信息，降低置信度（说明信息不足）
        if (response.contains("⚠️") || response.contains("无法获取") || 
            response.contains("暂时无法")) {
            baseConfidence -= 0.2;
        }
        
        // 如果回答过于泛泛而谈，降低置信度
        if (response.contains("请查看") || response.contains("请联系") || 
            response.contains("请等待") || response.contains("请稍后")) {
            if (response.length() < 100) {
                baseConfidence -= 0.3;
            }
        }
        
        // 确保置信度在合理范围内
        return Math.max(0.1, Math.min(baseConfidence, 1.0));
    }
    
    /**
     * 规则引擎回复（当AI不可用时的备用方案）
     */
    private String ruleBasedResponse(String question, List<String> knowledgeBase, String databaseInfo) {
        String lowerQuestion = question.toLowerCase();
        
        // 优先使用数据库信息
        if (databaseInfo != null && !databaseInfo.trim().isEmpty() && 
            !databaseInfo.contains("数据库查询出现异常")) {
            
            // 如果有数据库信息，基于数据库信息回答
            if (lowerQuestion.contains("推荐") || lowerQuestion.contains("零食") || 
                lowerQuestion.contains("商品") || lowerQuestion.contains("有") ||
                lowerQuestion.contains("吗") || lowerQuestion.contains("么")) {
                
                // 检查是否有具体的商品信息
                if (databaseInfo.contains("热门商品：")) {
                    String popularSection = databaseInfo.substring(databaseInfo.indexOf("热门商品："));
                    return "根据我们的数据库查询结果，目前有以下商品可供选择：\n\n" + popularSection;
                } else if (databaseInfo.contains("饮料类商品：")) {
                    String drinkSection = databaseInfo.substring(databaseInfo.indexOf("饮料类商品："));
                    return "根据我们的数据库，目前有以下饮料类商品：\n\n" + drinkSection;
                } else if (databaseInfo.contains("零食类商品：")) {
                    String snackSection = databaseInfo.substring(databaseInfo.indexOf("零食类商品："));
                    return "根据我们的数据库，目前有以下零食类商品：\n\n" + snackSection;
                } else if (databaseInfo.contains("注意：数据库中目前没有找到")) {
                    return "根据我们的数据库查询结果，" + databaseInfo.substring(databaseInfo.indexOf("注意："));
                } else {
                    return "根据我们的数据库查询结果，目前没有找到您询问的商品类型。\n\n" +
                           "建议您：\n" +
                           "1. 查看商品列表页面\n" +
                           "2. 使用搜索功能\n" +
                           "3. 转人工客服获取最新商品信息";
                }
            }
            
            // 订单相关问题
            if (lowerQuestion.contains("订单") || lowerQuestion.contains("查询") || 
                lowerQuestion.contains("没收到") || lowerQuestion.contains("发货")) {
                
                if (databaseInfo.contains("订单查询信息")) {
                    return "根据我们的数据库查询结果：\n\n" + databaseInfo;
                } else {
                    return "我无法获取您的订单信息。建议您：\n" +
                           "1. 登录系统查看\"我的订单\"页面\n" +
                           "2. 确认订单号是否正确\n" +
                           "3. 转人工客服获取帮助";
                 }
             }
             
             // 如果数据库信息不足，提供通用指导
             return "根据我们的数据库信息：\n\n" + databaseInfo + "\n\n" +
                    "如果以上信息无法解决您的问题，建议您转人工客服获取进一步帮助。";
         }
         
         // 如果没有数据库信息，提供严格的限制说明
         if (lowerQuestion.contains("推荐") || lowerQuestion.contains("零食") || 
             lowerQuestion.contains("商品") || lowerQuestion.contains("有") ||
             lowerQuestion.contains("吗") || lowerQuestion.contains("么")) {
             return "⚠️ 由于系统暂时无法获取数据库信息，我无法提供准确的商品信息。\n\n" +
                    "为了确保信息准确性，我建议您：\n" +
                    "1. 直接查看商品列表页面\n" +
                    "2. 使用搜索功能查找具体商品\n" +
                    "3. 转人工客服获取最新商品信息\n\n" +
                    "我不会推荐任何未经数据库验证的商品信息。";
         }
         
         if (lowerQuestion.contains("订单") || lowerQuestion.contains("查询") || 
             lowerQuestion.contains("没收到") || lowerQuestion.contains("发货")) {
             return "⚠️ 由于系统暂时无法获取数据库信息，我无法提供准确的订单信息。\n\n" +
                    "为了确保信息准确性，我建议您：\n" +
                    "1. 登录系统查看\"我的订单\"页面\n" +
                    "2. 确认订单号是否正确\n" +
                    "3. 转人工客服获取帮助\n\n" +
                    "我不会提供任何未经数据库验证的订单状态信息。";
         }
         
         // 如果有知识库信息，但明确说明仅供参考
         if (!knowledgeBase.isEmpty()) {
             return "⚠️ 注意：以下信息来自知识库，仅供参考。\n\n" +
                    knowledgeBase.get(0).split("\n答案：")[1] + "\n\n" +
                    "为了获取最新、最准确的信息，建议您：\n" +
                    "1. 直接查看相关页面\n" +
                    "2. 转人工客服获取帮助";
         }
         
         // 基于规则的通用回复（不涉及具体商品或订单信息）
         if (lowerQuestion.contains("退换货") || lowerQuestion.contains("退款")) {
             return "我们支持7天无理由退换货。在\"我的订单\"页面，找到要退款的订单，点击\"申请退款\"即可。";
         } else if (lowerQuestion.contains("配送") || lowerQuestion.contains("快递")) {
             return "我们使用顺丰、圆通等知名快递公司。同城1天内送达，省内1-2天，省外2-3天。满99元包邮哦！";
         } else if (lowerQuestion.contains("支付") || lowerQuestion.contains("支付宝")) {
             return "我们支持支付宝支付。在支付页面选择支付宝，扫码或输入密码即可完成支付。";
         } else if (lowerQuestion.contains("保质期") || lowerQuestion.contains("过期")) {
             return "零食类商品保质期一般在6-12个月，饮料类3-6个月。具体保质期请查看商品详情页。";
         } else {
             return "抱歉，我无法获取相关信息。建议您：\n" +
                    "1. 查看相关页面获取最新信息\n" +
                    "2. 转人工客服获取帮助\n\n" +
                    "为了确保信息准确性，我不会基于假设提供任何信息。";
         }
    }
    
    private void updateSessionStatus(String sessionId, double confidence) {
        java.util.Optional<ChatSession> sessionOpt = chatSessionRepository.findBySessionId(sessionId);
        if (sessionOpt.isPresent()) {
            ChatSession session = sessionOpt.get();
            session.setMessageCount(session.getMessageCount() + 1);
            session.setLastMessageAt(LocalDateTime.now());
            
            if (confidence < 0.6) {
                session.setStatus(ChatSession.SessionStatus.WAITING);
            }
            
            chatSessionRepository.save(session);
        }
    }

    /**
     * 从消息中提取订单号
     */
    private String extractOrderNumber(String message) {
        // 匹配订单号格式：O开头的18位字符（放宽到18-20位以增强容错）
        String pattern = "O[a-zA-Z0-9]{17,19}";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(message);
        
        if (m.find()) {
            return m.group();
        }
        
        // 如果没有找到标准格式，尝试提取可能的订单号
        String[] words = message.split("\\s+");
        for (String word : words) {
            if (word.length() >= 10 && word.matches(".*[a-zA-Z0-9].*")) {
                return word;
            }
        }
        
        return null;
    }

    /**
     * 从消息中提取可能的商品名称
     */
    private String[] extractPossibleProductNames(String message) {
        List<String> productNames = new ArrayList<>();
        
        // 1. 常见的饮料类商品
        String[] drinkProducts = {"咖啡", "奶茶", "果汁", "可乐", "雪碧", "芬达", "七喜", "纯净水", "矿泉水", "绿茶", "红茶", "乌龙茶"};
        
        // 2. 常见的零食类商品
        String[] snackProducts = {"果干", "坚果", "开心果", "杏仁", "松子", "腰果", "夏威夷果", "糖果", "巧克力", "薯片", "饼干", "泡面", "方便面"};
        
        // 3. 检查消息中是否包含这些商品名称
        for (String product : drinkProducts) {
            if (message.contains(product)) {
                productNames.add(product);
            }
        }
        
        for (String product : snackProducts) {
            if (message.contains(product)) {
                productNames.add(product);
            }
        }
        
        // 4. 如果没有找到具体商品名称，尝试提取通用词汇
        if (productNames.isEmpty()) {
            if (message.contains("咖啡")) productNames.add("咖啡");
            if (message.contains("奶茶")) productNames.add("奶茶");
            if (message.contains("果汁")) productNames.add("果汁");
            if (message.contains("零食")) productNames.add("零食");
            if (message.contains("饮料")) productNames.add("饮料");
        }
        
        return productNames.toArray(new String[0]);
    }

    /**
     * 从消息中提取可能的商家名称
     */
    private String[] extractPossibleMerchantNames(String message) {
        List<String> merchantNames = new ArrayList<>();
        
        // 1. 直接提取引号内的内容（如"真的好好吃"）
        java.util.regex.Pattern quotePattern = java.util.regex.Pattern.compile("\"([^\"]+)\"");
        java.util.regex.Matcher quoteMatcher = quotePattern.matcher(message);
        while (quoteMatcher.find()) {
            String found = quoteMatcher.group(1);
            if (found.length() >= 2) {
                merchantNames.add(found);
            }
        }
        
        // 2. 提取"这个商家"前面的内容（如"真的好好吃这个商家都有什么商品"）
        if (message.contains("这个商家")) {
            String before = message.substring(0, message.indexOf("这个商家")).trim();
            if (before.length() >= 2) {
                merchantNames.add(before);
                log.info("提取到商家名称: '{}' (从'这个商家'前面)", before);
            }
        }
        
        // 3. 提取"都有什么"前面的内容（如"真的好好吃都有什么商品"）
        if (message.contains("都有什么")) {
            String before = message.substring(0, message.indexOf("都有什么")).trim();
            if (before.length() >= 2) {
                merchantNames.add(before);
                log.info("提取到商家名称: '{}' (从'都有什么'前面)", before);
            }
        }
        
        // 4. 提取"有什么商品"前面的内容（如"真的好好吃有什么商品"）
        if (message.contains("有什么商品")) {
            String before = message.substring(0, message.indexOf("有什么商品")).trim();
            if (before.length() >= 2) {
                merchantNames.add(before);
                log.info("提取到商家名称: '{}' (从'有什么商品'前面)", before);
            }
        }
        
        // 5. 提取"csyh"这样的商家标识符
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b[a-zA-Z0-9]{3,10}\\b");
        java.util.regex.Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String found = matcher.group();
            // 不过滤任何商家标识符
            if (found.length() >= 3) {
                merchantNames.add(found);
                log.info("提取到商家标识符: '{}'", found);
            }
        }
        
        // 6. 如果没有找到具体商家名称，尝试提取通用词汇
        if (merchantNames.isEmpty()) {
            if (message.contains("商家")) merchantNames.add("商家");
            if (message.contains("店铺")) merchantNames.add("店铺");
            if (message.contains("店")) merchantNames.add("店");
            if (message.contains("商")) merchantNames.add("商");
        }
        
        // 去重并记录日志
        List<String> uniqueNames = merchantNames.stream().distinct().collect(Collectors.toList());
        log.info("最终提取的商家名称: {}", uniqueNames);
        
        return uniqueNames.toArray(new String[0]);
    }
}
