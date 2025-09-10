package com.example.shop.controller;

import com.example.shop.common.PageResult;
import com.example.shop.common.Result;
import com.example.shop.dto.ProductCreateRequest;
import com.example.shop.dto.ProductUpdateRequest;
import com.example.shop.dto.CategoryCreateRequest;
import com.example.shop.dto.CategoryUpdateRequest;
import com.example.shop.entity.*;
import com.example.shop.service.MerchantService;
import com.example.shop.vo.MerchantStatistics;
import com.example.shop.dto.MerchantUpdateProfileRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.example.shop.dto.ShipmentRequest;
import com.example.shop.dto.LogisticsTrackRequest;
import com.example.shop.entity.LogisticsTrack;

import java.util.List;

@RestController
@RequestMapping("/merchant")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    /**
     * 获取商家商品列表
     */
    @GetMapping("/products")
    public Result<PageResult<Product>> getProducts(Authentication auth,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer size,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) Long categoryId,
                                                  @RequestParam(required = false) String status) {
        Page<Product> products = merchantService.getProducts(auth.getName(), page, size, keyword, categoryId, status);
        return Result.success(new PageResult<>(products.getTotalElements(), products.getTotalPages(), page, size, products.getContent()));
    }

    /**
     * 创建商品
     */
    @PostMapping("/products")
    public Result<Product> createProduct(Authentication auth, @Validated @RequestBody ProductCreateRequest request) {
        return Result.success(merchantService.createProduct(auth.getName(), request));
    }

    /**
     * 更新商品
     */
    @PutMapping("/products/{id}")
    public Result<Product> updateProduct(Authentication auth, @PathVariable Long id, @Validated @RequestBody ProductUpdateRequest request) {
        return Result.success(merchantService.updateProduct(auth.getName(), id, request));
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/products/{id}")
    public Result<Void> deleteProduct(Authentication auth, @PathVariable Long id) {
        merchantService.deleteProduct(auth.getName(), id);
        return Result.success();
    }

    /**
     * 上架/下架商品
     */
    @PostMapping("/products/{id}/toggle-status")
    public Result<Product> toggleProductStatus(Authentication auth, @PathVariable Long id) {
        return Result.success(merchantService.toggleProductStatus(auth.getName(), id));
    }

    /**
     * 获取商家订单列表
     */
    @GetMapping("/orders")
    public Result<PageResult<Order>> getOrders(Authentication auth,
                                              @RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer size,
                                              @RequestParam(required = false) String status) {
        Page<Order> orders = merchantService.getOrders(auth.getName(), page, size, status);
        return Result.success(new PageResult<>(orders.getTotalElements(), orders.getTotalPages(), page, size, orders.getContent()));
    }

    /**
     * 发货
     */
    @PostMapping("/orders/{orderId}/ship")
    public Result<Shipment> shipOrder(Authentication auth, @PathVariable Long orderId, @RequestBody ShipmentRequest request) {
        return Result.success(merchantService.shipOrder(auth.getName(), orderId, request));
    }

    /** 商家追加物流轨迹 */
    @PostMapping("/logistics/{shipmentId}/tracks")
    public Result<LogisticsTrack> addLogisticsTrack(Authentication auth,
                                                    @PathVariable Long shipmentId,
                                                    @RequestBody LogisticsTrackRequest req) {
        return Result.success(merchantService.addLogisticsTrack(auth.getName(), shipmentId, req.getLocation(), req.getDescription(), req.getTrackTime()));
    }

    @PostMapping("/orders/{orderId}/close")
    public Result<Order> closeOrder(Authentication auth, @PathVariable Long orderId) {
        return Result.success(merchantService.closeOrder(auth.getName(), orderId));
    }

    /**
     * 获取商家统计信息
     */
    @GetMapping("/statistics")
    public Result<MerchantStatistics> getStatistics(Authentication auth) {
        return Result.success(merchantService.getStatistics(auth.getName()));
    }

    /** 统计：趋势 */
    @GetMapping("/statistics/trend")
    public Result<java.util.Map<String, Object>> trend(Authentication auth, @RequestParam(defaultValue = "30") Integer days) {
        return Result.success(merchantService.trend(auth.getName(), days));
    }

    /** 统计：Top商品 */
    @GetMapping("/statistics/product-top")
    public Result<java.util.Map<String, Object>> topProducts(Authentication auth) {
        return Result.success(merchantService.topProducts(auth.getName()));
    }

    /** 商家资料 */
    @GetMapping("/profile")
    public Result<Merchant> getProfile(Authentication auth) {
        return Result.success(merchantService.getProfile(auth.getName()));
    }

    @PutMapping("/profile")
    public Result<Merchant> updateProfile(Authentication auth, @Validated @RequestBody MerchantUpdateProfileRequest req) {
        return Result.success(merchantService.updateProfile(auth.getName(), req));
    }

    /**
     * 获取商品分类树
     */
    @GetMapping("/categories/tree")
    public Result<List<Category>> getCategoryTree() {
        return Result.success(merchantService.getCategoryTree());
    }

    @PostMapping("/categories")
    public Result<Category> createCategory(@Validated @RequestBody CategoryCreateRequest request) {
        return Result.success(merchantService.createCategory(request));
    }

    @PutMapping("/categories/{id}")
    public Result<Category> updateCategory(@PathVariable Long id, @Validated @RequestBody CategoryUpdateRequest request) {
        return Result.success(merchantService.updateCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        merchantService.deleteCategory(id);
        return Result.success();
    }
}


