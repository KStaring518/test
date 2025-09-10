package com.example.shop.service;

import com.example.shop.common.BusinessException;
import com.example.shop.dto.ProductCreateRequest;
import com.example.shop.dto.ProductUpdateRequest;
import com.example.shop.dto.CategoryCreateRequest;
import com.example.shop.dto.CategoryUpdateRequest;
import com.example.shop.entity.*;
import com.example.shop.repository.*;
import com.example.shop.vo.MerchantStatistics;
import com.example.shop.dto.MerchantUpdateProfileRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MerchantService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private LogisticsTrackRepository logisticsTrackRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    /*** 统计：商家近N日趋势（GMV与订单数） */
    public java.util.Map<String, Object> trend(String username, int days) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
        Merchant merchant = merchantRepository.findByUserId(user.getId()).orElseThrow(() -> new BusinessException("商家信息不存在"));

        days = days <= 0 ? 30 : Math.min(days, 60);
        java.util.List<String> labels = new java.util.ArrayList<>();
        java.util.List<java.math.BigDecimal> gmvs = new java.util.ArrayList<>();
        java.util.List<Long> orders = new java.util.ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            java.time.LocalDate d = java.time.LocalDate.now().minusDays(i);
            java.time.LocalDateTime start = d.atStartOfDay();
            java.time.LocalDateTime end = d.atTime(23,59,59);
            labels.add(d.toString());
            Long cnt = orderRepository.countByMerchantIdAndCreatedAtBetween(merchant.getId(), start, end);
            orders.add(cnt == null ? 0L : cnt);
            java.math.BigDecimal gmv = orderRepository.sumTotalAmountByMerchantIdAndStatusAndCreatedAtBetween(merchant.getId(), Order.OrderStatus.PAID, start, end)
                    .add(orderRepository.sumTotalAmountByMerchantIdAndStatusAndCreatedAtBetween(merchant.getId(), Order.OrderStatus.SHIPPED, start, end))
                    .add(orderRepository.sumTotalAmountByMerchantIdAndStatusAndCreatedAtBetween(merchant.getId(), Order.OrderStatus.FINISHED, start, end));
            gmvs.add(gmv);
        }
        java.util.Map<String, Object> res = new java.util.HashMap<>();
        res.put("labels", labels);
        res.put("gmv", gmvs);
        res.put("orders", orders);
        return res;
    }

    /*** 统计：商家热销商品Top5 */
    public java.util.Map<String, Object> topProducts(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
        Merchant merchant = merchantRepository.findByUserId(user.getId()).orElseThrow(() -> new BusinessException("商家信息不存在"));
        java.util.List<Object[]> rows = logisticsTrackRepository == null ? java.util.Collections.emptyList() : null;
        // 使用 OrderItemRepository
        java.util.List<Object[]> list = this.orderItemRepository.topProductsByMerchant(merchant.getId());
        java.util.List<String> labels = new java.util.ArrayList<>();
        java.util.List<Long> quantities = new java.util.ArrayList<>();
        int limit = Math.min(5, list.size());
        for (int i = 0; i < limit; i++) {
            Object[] r = list.get(i);
            labels.add((String) r[1]);
            quantities.add(((Number) r[2]).longValue());
        }
        java.util.Map<String, Object> res = new java.util.HashMap<>();
        res.put("labels", labels);
        res.put("values", quantities);
        return res;
    }

    /**
     * 获取商家商品列表
     */
    public Page<Product> getProducts(String username, Integer page, Integer size, String keyword, Long categoryId, String status) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
        Merchant merchant = merchantRepository.findByUserId(user.getId()).orElseThrow(() -> new BusinessException("商家信息不存在"));

        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        if (StringUtils.hasText(keyword) && categoryId != null && StringUtils.hasText(status)) {
            return productRepository.findByMerchantIdAndNameContainingAndCategoryIdAndStatus(merchant.getId(), keyword, categoryId, Product.ProductStatus.valueOf(status), pageRequest);
        } else if (StringUtils.hasText(keyword) && categoryId != null) {
            return productRepository.findByMerchantIdAndNameContainingAndCategoryId(merchant.getId(), keyword, categoryId, pageRequest);
        } else if (StringUtils.hasText(keyword) && StringUtils.hasText(status)) {
            return productRepository.findByMerchantIdAndNameContainingAndStatus(merchant.getId(), keyword, Product.ProductStatus.valueOf(status), pageRequest);
        } else if (categoryId != null && StringUtils.hasText(status)) {
            return productRepository.findByMerchantIdAndCategoryIdAndStatus(merchant.getId(), categoryId, Product.ProductStatus.valueOf(status), pageRequest);
        } else if (StringUtils.hasText(keyword)) {
            return productRepository.findByMerchantIdAndNameContaining(merchant.getId(), keyword, pageRequest);
        } else if (categoryId != null) {
            return productRepository.findByMerchantIdAndCategoryId(merchant.getId(), categoryId, pageRequest);
        } else if (StringUtils.hasText(status)) {
            return productRepository.findByMerchantIdAndStatus(merchant.getId(), Product.ProductStatus.valueOf(status), pageRequest);
        } else {
            return productRepository.findByMerchantId(merchant.getId(), pageRequest);
        }
    }

    /**
     * 创建商品
     */
    @Transactional
    public Product createProduct(String username, ProductCreateRequest request) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
        Merchant merchant = merchantRepository.findByUserId(user.getId()).orElseThrow(() -> new BusinessException("商家信息不存在"));
        
        Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow(() -> new BusinessException("商品分类不存在"));

        Product product = new Product();
        product.setName(request.getName());
        product.setSubtitle(request.getSubtitle());
        product.setCategory(category);
        product.setMerchant(merchant);
        product.setCoverImage(request.getCoverImage());
        product.setPrice(request.getPrice());
        product.setUnit(request.getUnit());
        product.setStock(request.getStock());
        product.setDescription(request.getDescription());
        product.setStatus(Product.ProductStatus.ON_SALE);

        return productRepository.save(product);
    }

    /**
     * 更新商品
     */
    @Transactional
    public Product updateProduct(String username, Long productId, ProductUpdateRequest request) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
        Merchant merchant = merchantRepository.findByUserId(user.getId()).orElseThrow(() -> new BusinessException("商家信息不存在"));
        
        Product product = productRepository.findByIdAndMerchantId(productId, merchant.getId())
                .orElseThrow(() -> new BusinessException("商品不存在"));
        
        Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow(() -> new BusinessException("商品分类不存在"));

        product.setName(request.getName());
        product.setSubtitle(request.getSubtitle());
        product.setCategory(category);
        product.setCoverImage(request.getCoverImage());
        product.setPrice(request.getPrice());
        product.setUnit(request.getUnit());
        product.setStock(request.getStock());
        product.setDescription(request.getDescription());
        
        // 添加调试日志
        System.out.println("更新商品图片URL: " + request.getCoverImage());
        System.out.println("商品ID: " + productId);

        return productRepository.save(product);
    }

    /**
     * 删除商品
     */
    @Transactional
    public void deleteProduct(String username, Long productId) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
        Merchant merchant = merchantRepository.findByUserId(user.getId()).orElseThrow(() -> new BusinessException("商家信息不存在"));
        
        Product product = productRepository.findByIdAndMerchantId(productId, merchant.getId())
                .orElseThrow(() -> new BusinessException("商品不存在"));
        
        productRepository.delete(product);
    }

    /**
     * 上架/下架商品
     */
    @Transactional
    public Product toggleProductStatus(String username, Long productId) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
        Merchant merchant = merchantRepository.findByUserId(user.getId()).orElseThrow(() -> new BusinessException("商家信息不存在"));
        
        Product product = productRepository.findByIdAndMerchantId(productId, merchant.getId())
                .orElseThrow(() -> new BusinessException("商品不存在"));
        
        if (product.getStatus() == Product.ProductStatus.ON_SALE) {
            product.setStatus(Product.ProductStatus.OFF_SALE);
        } else {
            product.setStatus(Product.ProductStatus.ON_SALE);
        }
        
        return productRepository.save(product);
    }

    /**
     * 获取商家订单列表
     */
    public Page<Order> getOrders(String username, Integer page, Integer size, String status) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
        Merchant merchant = merchantRepository.findByUserId(user.getId()).orElseThrow(() -> new BusinessException("商家信息不存在"));

        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        if (StringUtils.hasText(status)) {
            return orderRepository.findByMerchantIdAndStatus(merchant.getId(), Order.OrderStatus.valueOf(status), pageRequest);
        } else {
            return orderRepository.findByMerchantId(merchant.getId(), pageRequest);
        }
    }

    /**
     * 发货
     */
    @Transactional
    public Shipment shipOrder(String username, Long orderId, com.example.shop.dto.ShipmentRequest req) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
        Merchant merchant = merchantRepository.findByUserId(user.getId()).orElseThrow(() -> new BusinessException("商家信息不存在"));
        
        Order order = orderRepository.findByIdAndMerchantId(orderId, merchant.getId())
                .orElseThrow(() -> new BusinessException("订单不存在"));
        
        if (order.getStatus() != Order.OrderStatus.PAID) {
            throw new BusinessException("只有已支付的订单才能发货");
        }

        Shipment shipment = new Shipment();
        shipment.setOrder(order);
        shipment.setShipmentNo("S" + UUID.randomUUID().toString().replace("-", "").substring(0, 18));
        shipment.setCarrier(req.getCarrier());
        shipment.setTrackingNo(req.getTrackingNo());
        shipment.setStatus(Shipment.ShipmentStatus.SHIPPED);
        shipment.setShippedAt(LocalDateTime.now());
        // 使用商家选择的发货地址；若未传则兜底为订单收货地址
        shipment.setShippingAddress(req.getSenderAddress() != null ? req.getSenderAddress() : order.getReceiverAddress());
        shipment.setSenderName(req.getSenderName());
        shipment.setSenderPhone(req.getSenderPhone());
        Shipment saved = shipmentRepository.save(shipment);

        // 模拟第一条物流轨迹
        // 初始轨迹：商家仓库（确保时间最早）
        LogisticsTrack track = new LogisticsTrack();
        track.setShipment(saved);
        track.setLocation("商家仓库");
        track.setDescription("包裹已出库");
        track.setTrackTime(saved.getShippedAt());
        logisticsTrackRepository.save(track);

        order.setStatus(Order.OrderStatus.SHIPPED);
        orderRepository.save(order);
        
        return saved;
    }

    /** 商家追加物流轨迹（校验订单归属） */
    @Transactional
    public LogisticsTrack addLogisticsTrack(String username, Long shipmentId, String location, String description, LocalDateTime trackTime) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
        Merchant merchant = merchantRepository.findByUserId(user.getId()).orElseThrow(() -> new BusinessException("商家信息不存在"));

        Shipment shipment = shipmentRepository.findById(shipmentId).orElseThrow(() -> new BusinessException("发货单不存在"));
        if (shipment.getOrder() == null || shipment.getOrder().getMerchant() == null || !shipment.getOrder().getMerchant().getId().equals(merchant.getId())) {
            throw new BusinessException("无权操作该订单的物流");
        }

        LogisticsTrack t = new LogisticsTrack();
        t.setShipment(shipment);
        t.setLocation(location);
        t.setDescription(description);
        t.setTrackTime(trackTime != null ? trackTime : LocalDateTime.now());
        return logisticsTrackRepository.save(t);
    }

    /**
     * 关闭订单（仅未支付）
     */
    @Transactional
    public Order closeOrder(String username, Long orderId) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
        Merchant merchant = merchantRepository.findByUserId(user.getId()).orElseThrow(() -> new BusinessException("商家信息不存在"));

        Order order = orderRepository.findByIdAndMerchantId(orderId, merchant.getId())
                .orElseThrow(() -> new BusinessException("订单不存在"));

        if (order.getStatus() != Order.OrderStatus.UNPAID) {
            throw new BusinessException("仅未支付订单可关闭");
        }
        order.setStatus(Order.OrderStatus.CLOSED);
        return orderRepository.save(order);
    }

    /**
     * 获取商家统计信息
     */
    public MerchantStatistics getStatistics(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
        Merchant merchant = merchantRepository.findByUserId(user.getId()).orElseThrow(() -> new BusinessException("商家信息不存在"));

        int totalProducts = productRepository.countByMerchantId(merchant.getId());
        int onSaleProducts = productRepository.countByMerchantIdAndStatus(merchant.getId(), Product.ProductStatus.ON_SALE);
        int totalOrders = orderRepository.countByMerchantId(merchant.getId());
        int pendingOrders = orderRepository.countByMerchantIdAndStatus(merchant.getId(), Order.OrderStatus.PAID);
        int shippedOrders = orderRepository.countByMerchantIdAndStatus(merchant.getId(), Order.OrderStatus.SHIPPED);
        
        BigDecimal totalRevenue = orderRepository.sumTotalAmountByMerchantIdAndStatus(merchant.getId(), Order.OrderStatus.FINISHED);
        BigDecimal todayRevenue = orderRepository.sumTotalAmountByMerchantIdAndStatusAndCreatedAtBetween(
                merchant.getId(), 
                Order.OrderStatus.FINISHED, 
                LocalDate.now().atStartOfDay(), 
                LocalDate.now().atTime(23, 59, 59)
        );

        return new MerchantStatistics(
                totalProducts,
                onSaleProducts,
                totalOrders,
                pendingOrders,
                shippedOrders,
                totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                todayRevenue != null ? todayRevenue : BigDecimal.ZERO
        );
    }

    /** 获取商家资料 */
    public Merchant getProfile(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
        return merchantRepository.findByUserId(user.getId()).orElseThrow(() -> new BusinessException("商家信息不存在"));
    }

    /** 更新商家资料 */
    @Transactional
    public Merchant updateProfile(String username, MerchantUpdateProfileRequest req) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
        Merchant merchant = merchantRepository.findByUserId(user.getId()).orElseThrow(() -> new BusinessException("商家信息不存在"));
        if (req.getShopName() != null) merchant.setShopName(req.getShopName());
        if (req.getShopDescription() != null) merchant.setShopDescription(req.getShopDescription());
        if (req.getContactPhone() != null) merchant.setContactPhone(req.getContactPhone());
        if (req.getBusinessLicense() != null) merchant.setBusinessLicense(req.getBusinessLicense());
        return merchantRepository.save(merchant);
    }

    /**
     * 获取商品分类树
     */
    public List<Category> getCategoryTree() {
        return categoryRepository.findEnabledCategories();
    }

    /**
     * 创建分类（商家端：简单共享平台分类，不区分商家）
     */
    @Transactional
    public Category createCategory(CategoryCreateRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setSortOrder(request.getSortOrder());
        category.setStatus(Category.CategoryStatus.ENABLED);
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new BusinessException("父分类不存在"));
            category.setParent(parent);
        }
        return categoryRepository.save(category);
    }

    /**
     * 更新分类
     */
    @Transactional
    public Category updateCategory(Long categoryId, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException("分类不存在"));
        category.setName(request.getName());
        category.setSortOrder(request.getSortOrder());
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new BusinessException("父分类不存在"));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }
        return categoryRepository.save(category);
    }

    /**
     * 删除分类
     */
    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException("分类不存在"));
        categoryRepository.delete(category);
    }
}


