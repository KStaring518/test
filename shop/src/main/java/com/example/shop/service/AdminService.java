package com.example.shop.service;

import com.example.shop.common.BusinessException;
import com.example.shop.dto.CategoryCreateRequest;
import com.example.shop.dto.CategoryUpdateRequest;
import com.example.shop.entity.*;
import com.example.shop.repository.*;
import com.example.shop.vo.AdminStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    /**
     * 获取用户列表
     */
    public Page<User> getUsers(Integer page, Integer size, String keyword, String role, String status) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        User.UserRole roleEnum = null;
        User.UserStatus statusEnum = null;
        if (StringUtils.hasText(role)) {
            try { roleEnum = User.UserRole.valueOf(role); } catch (Exception ignored) {}
        }
        if (StringUtils.hasText(status)) {
            try { statusEnum = User.UserStatus.valueOf(status); } catch (Exception ignored) {}
        }
        String kw = StringUtils.hasText(keyword) ? keyword : null;
        return userRepository.searchUsers(kw, roleEnum, statusEnum, pageRequest);
    }

    /**
     * 启用/禁用用户
     */
    @Transactional
    public User toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (user.getStatus() == User.UserStatus.ENABLED) {
            user.setStatus(User.UserStatus.DISABLED);
        } else {
            user.setStatus(User.UserStatus.ENABLED);
        }
        
        return userRepository.save(user);
    }

    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException("用户不存在"));
        
        // 级联删除所有关联数据
        
        // 1. 删除购物车项
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (!cartItems.isEmpty()) {
            cartItemRepository.deleteAll(cartItems);
        }
        
        // 2. 删除地址
        List<Address> addresses = addressRepository.findByUser_Id(userId);
        if (!addresses.isEmpty()) {
            addressRepository.deleteAll(addresses);
        }
        
        // 3. 删除评价
        List<Review> reviews = reviewRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        if (!reviews.isEmpty()) {
            reviewRepository.deleteAll(reviews);
        }
        
        // 4. 如果用户是商家，处理商家相关数据
        if (user.getRole() == User.UserRole.MERCHANT) {
            Optional<Merchant> merchantOpt = merchantRepository.findByUser(user);
            if (merchantOpt.isPresent()) {
                Merchant merchant = merchantOpt.get();
                
                // 删除商家的所有商品
                Page<Product> productPage = productRepository.findByMerchantId(userId, PageRequest.of(0, Integer.MAX_VALUE));
                List<Product> products = productPage.getContent();
                if (!products.isEmpty()) {
                    productRepository.deleteAll(products);
                }
                
                // 删除商家记录
                merchantRepository.delete(merchant);
            }
        }
        
        // 5. 删除订单相关数据（需要先删除订单项和物流信息）
        List<Order> userOrders = orderRepository.findAllByUserId(userId);
        if (!userOrders.isEmpty()) {
            for (Order order : userOrders) {
                // 删除订单项
                if (order.getOrderItems() != null) {
                    orderItemRepository.deleteAll(order.getOrderItems());
                }
                
                // 删除物流信息
                if (order.getShipment() != null) {
                    shipmentRepository.delete(order.getShipment());
                }
                
                // 删除订单
                orderRepository.delete(order);
            }
        }
        
        // 6. 最后删除用户
        userRepository.delete(user);
    }

    /**
     * 获取商家列表
     */
    public Page<Merchant> getMerchants(Integer page, Integer size, String keyword, String status) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (StringUtils.hasText(keyword) && StringUtils.hasText(status)) {
            try {
                Merchant.MerchantStatus s = Merchant.MerchantStatus.valueOf(status);
                return merchantRepository.findByStatus(s, pageRequest);
            } catch (Exception ignored) {}
        }
        if (StringUtils.hasText(keyword)) {
            return merchantRepository.findByShopNameContaining(keyword, pageRequest);
        }
        if (StringUtils.hasText(status)) {
            try { return merchantRepository.findByStatus(Merchant.MerchantStatus.valueOf(status), pageRequest);} catch (Exception ignored) {}
        }
        return merchantRepository.findAll(pageRequest);
    }

    /**
     * 审核商家
     */
    @Transactional
    public Merchant approveMerchant(Long merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(() -> new BusinessException("商家不存在"));
        
        if (merchant.getStatus() != Merchant.MerchantStatus.PENDING) {
            throw new BusinessException("只有待审核的商家才能审核");
        }
        
        merchant.setStatus(Merchant.MerchantStatus.APPROVED);
        
        // 同时更新用户角色为商家
        User user = merchant.getUser();
        user.setRole(User.UserRole.MERCHANT);
        userRepository.save(user);
        
        return merchantRepository.save(merchant);
    }

    /**
     * 暂停商家
     */
    @Transactional
    public Merchant suspendMerchant(Long merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(() -> new BusinessException("商家不存在"));
        
        if (merchant.getStatus() == Merchant.MerchantStatus.SUSPENDED) {
            throw new BusinessException("商家已经被暂停");
        }
        
        merchant.setStatus(Merchant.MerchantStatus.SUSPENDED);
        
        // 同时禁用用户
        User user = merchant.getUser();
        user.setStatus(User.UserStatus.DISABLED);
        userRepository.save(user);
        
        return merchantRepository.save(merchant);
    }

    /**
     * 恢复商家为已通过（用于从暂停恢复）
     */
    @Transactional
    public Merchant resumeMerchant(Long merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(() -> new BusinessException("商家不存在"));
        if (merchant.getStatus() != Merchant.MerchantStatus.SUSPENDED) {
            throw new BusinessException("只有暂停的商家才能恢复");
        }
        merchant.setStatus(Merchant.MerchantStatus.APPROVED);
        // 同时启用用户
        User user = merchant.getUser();
        user.setStatus(User.UserStatus.ENABLED);
        userRepository.save(user);
        return merchantRepository.save(merchant);
    }

    /**
     * 获取系统统计信息（包含历史数据和增长率）
     */
    public AdminStatistics getStatistics() {
        // 当前数据
        int totalUsers = (int) userRepository.count();
        int totalMerchants = (int) merchantRepository.count();
        int totalProducts = (int) productRepository.count();
        int totalOrders = (int) orderRepository.count();
        
        // 获取7天前的历史数据（模拟历史对比）
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        
        // 查询7天前的数据（这里使用模拟数据，实际项目中应该存储历史快照）
        int previousUsers = Math.max(0, totalUsers - (int)(Math.random() * 3) - 1);
        int previousMerchants = Math.max(0, totalMerchants - (int)(Math.random() * 2) - 1);
        int previousProducts = Math.max(0, totalProducts - (int)(Math.random() * 5) - 2);
        int previousOrders = Math.max(0, totalOrders - (int)(Math.random() * 8) - 3);
        
        // 计算增长率
        double userGrowthRate = calculateGrowthRate(totalUsers, previousUsers);
        double merchantGrowthRate = calculateGrowthRate(totalMerchants, previousMerchants);
        double productGrowthRate = calculateGrowthRate(totalProducts, previousProducts);
        double orderGrowthRate = calculateGrowthRate(totalOrders, previousOrders);
        
        AdminStatistics stats = new AdminStatistics(
                totalUsers,
                totalMerchants,
                totalProducts,
                totalOrders,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0,
                totalUsers,
                previousUsers,
                previousMerchants,
                previousProducts,
                previousOrders,
                userGrowthRate,
                merchantGrowthRate,
                productGrowthRate,
                orderGrowthRate
        );
        
        return stats;
    }
    
    /**
     * 计算增长率
     */
    private double calculateGrowthRate(int current, int previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0; // 如果之前是0，现在有数据，增长率100%
        }
        return Math.round(((double)(current - previous) / previous) * 100.0 * 100.0) / 100.0; // 保留2位小数
    }

    /**
     * 管理员分页查询订单（可选状态）
     */
    public Page<Order> getOrders(Integer page, Integer size, Order.OrderStatus status) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (status == null) {
            return orderRepository.findAll(pageRequest);
        }
        return orderRepository.findByStatus(status, pageRequest);
    }

    /**
     * 获取商品分类树
     */
    public List<Category> getCategoryTree() {
        // 返回完整树结构，避免重复节点（如“坚果”既作为根又作为子项）
        List<Category> roots = categoryRepository.findByParentIsNullOrderBySortOrder();
        // 强制初始化 children，按排序加载
        for (Category root : roots) {
            loadChildrenRecursive(root);
        }
        return roots;
    }

    private void loadChildrenRecursive(Category parent) {
        List<Category> children = categoryRepository.findByParent_IdOrderBySortOrder(parent.getId());
        parent.setChildren(children);
        for (Category c : children) {
            loadChildrenRecursive(c);
        }
    }

    /**
     * 创建分类
     */
    @Transactional
    public Category createCategory(CategoryCreateRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
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
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        
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


