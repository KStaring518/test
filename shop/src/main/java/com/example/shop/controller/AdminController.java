package com.example.shop.controller;

import com.example.shop.common.PageResult;
import com.example.shop.common.Result;
import com.example.shop.dto.CategoryCreateRequest;
import com.example.shop.dto.CategoryUpdateRequest;
import com.example.shop.entity.*;
import com.example.shop.service.AdminService;
import com.example.shop.vo.AdminStatistics;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.OrderItemRepository;
import com.example.shop.vo.SystemConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    /**
     * 获取用户列表
     */
    @GetMapping("/users")
    public Result<PageResult<User>> getUsers(@RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "10") Integer size,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) String role,
                                           @RequestParam(required = false) String status) {
        Page<User> users = adminService.getUsers(page, size, keyword, role, status);
        return Result.success(new PageResult<>(users.getTotalElements(), users.getTotalPages(), page, size, users.getContent()));
    }

    /**
     * 启用/禁用用户
     */
    @PostMapping("/users/{id}/toggle-status")
    public Result<User> toggleUserStatus(@PathVariable Long id) {
        return Result.success(adminService.toggleUserStatus(id));
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return Result.success();
    }

    /**
     * 获取商家列表
     */
    @GetMapping("/merchants")
    public Result<PageResult<Merchant>> getMerchants(@RequestParam(defaultValue = "1") Integer page,
                                                    @RequestParam(defaultValue = "10") Integer size,
                                                    @RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) String status) {
        Page<Merchant> merchants = adminService.getMerchants(page, size, keyword, status);
        return Result.success(new PageResult<>(merchants.getTotalElements(), merchants.getTotalPages(), page, size, merchants.getContent()));
    }

    /**
     * 审核商家
     */
    @PostMapping("/merchants/{id}/approve")
    public Result<Merchant> approveMerchant(@PathVariable Long id) {
        return Result.success(adminService.approveMerchant(id));
    }

    /**
     * 暂停商家
     */
    @PostMapping("/merchants/{id}/suspend")
    public Result<Merchant> suspendMerchant(@PathVariable Long id) {
        return Result.success(adminService.suspendMerchant(id));
    }

    /**
     * 恢复商家（从暂停恢复为已通过）
     */
    @PostMapping("/merchants/{id}/resume")
    public Result<Merchant> resumeMerchant(@PathVariable Long id) {
        return Result.success(adminService.resumeMerchant(id));
    }

    /**
     * 获取系统统计信息
     */
    @GetMapping("/statistics")
    public Result<AdminStatistics> getStatistics() {
        return Result.success(adminService.getStatistics());
    }

    /**
     * 订单分页（管理员）
     */
    @GetMapping("/orders")
    public Result<PageResult<com.example.shop.dto.AdminOrderListDTO>> getOrders(@RequestParam(defaultValue = "1") Integer page,
                                                                                @RequestParam(defaultValue = "10") Integer size,
                                                                                @RequestParam(required = false) Order.OrderStatus status) {
        Page<Order> p = adminService.getOrders(page, size, status);
        java.util.List<com.example.shop.dto.AdminOrderListDTO> list = p.map(com.example.shop.dto.AdminOrderListDTO::fromOrder).getContent();
        return Result.success(new PageResult<>(p.getTotalElements(), p.getTotalPages(), page, size, list));
    }

    /**
     * 获取商品分类树
     */
    @GetMapping("/categories/tree")
    public Result<List<Category>> getCategoryTree() {
        return Result.success(adminService.getCategoryTree());
    }

    /**
     * 创建分类
     */
    @PostMapping("/categories")
    public Result<Category> createCategory(@Validated @RequestBody CategoryCreateRequest request) {
        return Result.success(adminService.createCategory(request));
    }

    /**
     * 更新分类
     */
    @PutMapping("/categories/{id}")
    public Result<Category> updateCategory(@PathVariable Long id, @Validated @RequestBody CategoryUpdateRequest request) {
        return Result.success(adminService.updateCategory(id, request));
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
        return Result.success();
    }

    /**
     * 系统配置（简化：内存返回/更新）
     */
    private SystemConfig cachedConfig = new SystemConfig("零食商城", "400-000-0000", "support@example.com", 30, 7);

    @GetMapping("/system/config")
    public Result<SystemConfig> getSystemConfig() {
        return Result.success(cachedConfig);
    }

    @PostMapping("/system/config")
    public Result<SystemConfig> saveSystemConfig(@RequestBody SystemConfig cfg) {
        this.cachedConfig = cfg;
        return Result.success(cfg);
    }

    // 统计概览（管理员）
    @GetMapping("/statistics/overview")
    public Result<Map<String, Object>> statisticsOverview() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(23,59,59);
        long todayOrders = orderRepository.countByTimeRange(start, end);
        BigDecimal todayGmv = orderRepository.sumTotalAmountByStatusAndCreatedAtBetween(Order.OrderStatus.PAID, start, end)
                .add(orderRepository.sumTotalAmountByStatusAndCreatedAtBetween(Order.OrderStatus.SHIPPED, start, end))
                .add(orderRepository.sumTotalAmountByStatusAndCreatedAtBetween(Order.OrderStatus.FINISHED, start, end));
        Map<String, Object> data = new HashMap<>();
        data.put("todayOrders", todayOrders);
        data.put("todayGmv", todayGmv);
        return Result.success(data);
    }

    // 趋势（管理员）
    @GetMapping("/statistics/trend")
    public Result<Map<String, Object>> statisticsTrend(@RequestParam(defaultValue = "7") Integer days) {
        days = (days == null || days <= 0) ? 7 : Math.min(days, 60);
        java.util.List<String> labels = new java.util.ArrayList<>();
        java.util.List<BigDecimal> gmvs = new java.util.ArrayList<>();
        java.util.List<Long> orders = new java.util.ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            LocalDateTime start = d.atStartOfDay();
            LocalDateTime end = d.atTime(23,59,59);
            labels.add(d.toString());
            long cnt = orderRepository.countByTimeRange(start, end);
            orders.add(cnt);
            BigDecimal gmv = orderRepository.sumTotalAmountByStatusAndCreatedAtBetween(Order.OrderStatus.PAID, start, end)
                    .add(orderRepository.sumTotalAmountByStatusAndCreatedAtBetween(Order.OrderStatus.SHIPPED, start, end))
                    .add(orderRepository.sumTotalAmountByStatusAndCreatedAtBetween(Order.OrderStatus.FINISHED, start, end));
            gmvs.add(gmv);
        }
        Map<String, Object> res = new HashMap<>();
        res.put("labels", labels);
        res.put("gmv", gmvs);
        res.put("orders", orders);
        return Result.success(res);
    }

    // 订单状态占比
    @GetMapping("/statistics/status-distribution")
    public Result<Map<String, Object>> statusDistribution() {
        Map<String, Object> res = new HashMap<>();
        java.util.List<String> labels = new java.util.ArrayList<>();
        java.util.List<Long> values = new java.util.ArrayList<>();
        for (Order.OrderStatus s : Order.OrderStatus.values()) {
            labels.add(s.name());
            Long cnt = orderRepository.countByStatus(s);
            values.add(cnt == null ? 0L : cnt);
        }
        res.put("labels", labels);
        res.put("values", values);
        return Result.success(res);
    }

    // 分类销售Top5
    @GetMapping("/statistics/category-top")
    public Result<Map<String, Object>> categoryTop() {
        java.util.List<Object[]> rows = orderItemRepository.sumAmountByCategoryTop();
        java.util.List<String> labels = new java.util.ArrayList<>();
        java.util.List<java.math.BigDecimal> values = new java.util.ArrayList<>();
        int limit = Math.min(5, rows.size());
        for (int i = 0; i < limit; i++) {
            Object[] r = rows.get(i);
            labels.add((String) r[0]);
            values.add((java.math.BigDecimal) r[1]);
        }
        Map<String, Object> res = new HashMap<>();
        res.put("labels", labels);
        res.put("values", values);
        return Result.success(res);
    }
}


