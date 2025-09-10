package com.example.shop.service;

import com.example.shop.entity.Product;
import com.example.shop.entity.Category;
import com.example.shop.repository.ProductRepository;
import com.example.shop.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ChatbotProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // 添加缓存机制，减少数据库查询负荷
    private final ConcurrentHashMap<String, CacheEntry> productCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CacheEntry> categoryCache = new ConcurrentHashMap<>();
    
    private static final long CACHE_DURATION = TimeUnit.MINUTES.toMillis(5); // 5分钟缓存
    
    private static class CacheEntry {
        final Object data;
        final long timestamp;
        
        CacheEntry(Object data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isValid() {
            return System.currentTimeMillis() - timestamp < CACHE_DURATION;
        }
    }

    /**
     * 根据关键词搜索商品（供智能客服使用）
     */
    public List<Product> searchProductsByKeyword(String keyword, int limit) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getPopularProducts(limit);
        }
        
        // 检查缓存
        String cacheKey = "search_" + keyword + "_" + limit;
        CacheEntry cached = productCache.get(cacheKey);
        if (cached != null && cached.isValid()) {
            return (List<Product>) cached.data;
        }
        
        // 搜索商品名称和副标题
        Pageable pageable = PageRequest.of(0, limit);
        List<Product> products = productRepository.searchByKeyword(keyword.trim(), pageable).getContent();
        
        // 过滤有效商品
        List<Product> filteredProducts = products.stream()
            .filter(product -> product.getMerchant() != null && 
                             product.getStatus() == Product.ProductStatus.ON_SALE)
            .collect(Collectors.toList());
        
        // 缓存结果
        productCache.put(cacheKey, new CacheEntry(filteredProducts));
        
        return filteredProducts;
    }

    /**
     * 根据分类获取商品（供智能客服使用）
     */
    public List<Product> getProductsByCategory(Long categoryId, int limit) {
        if (categoryId == null || categoryId <= 0) {
            return getPopularProducts(limit);
        }
        
        // 检查缓存
        String cacheKey = "category_" + categoryId + "_" + limit;
        CacheEntry cached = productCache.get(cacheKey);
        if (cached != null && cached.isValid()) {
            return (List<Product>) cached.data;
        }
        
        // 获取分类下的商品
        Pageable pageable = PageRequest.of(0, limit);
        List<Product> products = productRepository.findByCategoryIdAndStatus(
            categoryId, Product.ProductStatus.ON_SALE, pageable).getContent();
        
        // 过滤有效商品
        List<Product> filteredProducts = products.stream()
            .filter(product -> product.getMerchant() != null)
            .collect(Collectors.toList());
        
        // 缓存结果
        productCache.put(cacheKey, new CacheEntry(filteredProducts));
        
        return filteredProducts;
    }

    /**
     * 获取热门商品（供智能客服使用）
     */
    public List<Product> getPopularProducts(int limit) {
        // 检查缓存
        String cacheKey = "popular_" + limit;
        CacheEntry cached = productCache.get(cacheKey);
        if (cached != null && cached.isValid()) {
            return (List<Product>) cached.data;
        }
        
        Pageable pageable = PageRequest.of(0, limit);
        List<Product> products = productRepository.findByStatus(Product.ProductStatus.ON_SALE, pageable).getContent();
        
        // 过滤有效商品并按创建时间排序
        List<Product> filteredProducts = products.stream()
            .filter(product -> product.getMerchant() != null)
            .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
            .collect(Collectors.toList());
        
        // 缓存结果
        productCache.put(cacheKey, new CacheEntry(filteredProducts));
        
        return filteredProducts;
    }

    /**
     * 获取所有启用的分类（供智能客服使用）
     */
    public List<Category> getAllEnabledCategories() {
        // 检查缓存
        String cacheKey = "categories";
        CacheEntry cached = categoryCache.get(cacheKey);
        if (cached != null && cached.isValid()) {
            return (List<Category>) cached.data;
        }
        
        List<Category> categories = categoryRepository.findEnabledCategories();
        
        // 缓存结果
        categoryCache.put(cacheKey, new CacheEntry(categories));
        
        return categories;
    }

    /**
     * 根据商品类型获取推荐商品（供智能客服使用）
     */
    public List<Product> getRecommendationsByType(String type, int limit) {
        String keyword = "";
        switch (type.toLowerCase()) {
            case "坚果":
            case "坚果类":
                keyword = "坚果";
                break;
            case "糖果":
            case "糖果类":
                keyword = "糖";
                break;
            case "饮料":
            case "饮品":
                keyword = "可乐";
                break;
            case "零食":
            case "小吃":
                keyword = "薯片";
                break;
            case "巧克力":
                keyword = "巧克力";
                break;
            default:
                return getPopularProducts(limit);
        }
        
        return searchProductsByKeyword(keyword, limit);
    }

    /**
     * 检查商品是否存在（供智能客服使用）
     */
    public boolean productExists(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            return false;
        }
        
        // 检查缓存
        String cacheKey = "exists_" + productName;
        CacheEntry cached = productCache.get(cacheKey);
        if (cached != null && cached.isValid()) {
            return (Boolean) cached.data;
        }
        
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = productRepository.searchByKeyword(productName.trim(), pageable).getContent();
        boolean exists = products.stream()
            .anyMatch(product -> product.getMerchant() != null && 
                               product.getStatus() == Product.ProductStatus.ON_SALE);
        
        // 缓存结果
        productCache.put(cacheKey, new CacheEntry(exists));
        
        return exists;
    }

    /**
     * 获取商品库存信息（供智能客服使用）
     */
    public String getProductStockInfo(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            return "商品名称不能为空";
        }
        
        // 检查缓存
        String cacheKey = "stock_" + productName;
        CacheEntry cached = productCache.get(cacheKey);
        if (cached != null && cached.isValid()) {
            return (String) cached.data;
        }
        
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = productRepository.searchByKeyword(productName.trim(), pageable).getContent();
        List<Product> validProducts = products.stream()
            .filter(product -> product.getMerchant() != null && 
                             product.getStatus() == Product.ProductStatus.ON_SALE)
            .collect(Collectors.toList());
        
        if (validProducts.isEmpty()) {
            String result = "抱歉，我们没有找到名为'" + productName + "'的商品";
            productCache.put(cacheKey, new CacheEntry(result));
            return result;
        }
        
        StringBuilder info = new StringBuilder();
        for (Product product : validProducts) {
            info.append("商品：").append(product.getName())
                .append("，价格：").append(product.getPrice()).append("元/").append(product.getUnit())
                .append("，库存：").append(product.getStock()).append("件");
            
            if (product.getStock() <= 0) {
                info.append("（已售罄）");
            } else if (product.getStock() < 10) {
                info.append("（库存紧张）");
            }
            info.append("\n");
        }
        
        String result = info.toString();
        productCache.put(cacheKey, new CacheEntry(result));
        return result;
    }
    
    /**
     * 清理过期缓存
     */
    public void clearExpiredCache() {
        productCache.entrySet().removeIf(entry -> !entry.getValue().isValid());
        categoryCache.entrySet().removeIf(entry -> !entry.getValue().isValid());
    }
    
    /**
     * 强制清理所有缓存
     */
    public void clearAllCache() {
        productCache.clear();
        categoryCache.clear();
    }
}
