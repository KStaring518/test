package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatistics {
    private Integer totalUsers;
    private Integer totalMerchants;
    private Integer totalProducts;
    private Integer totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
    private Integer pendingMerchants;
    private Integer activeUsers;
    
    // 添加历史数据字段，用于计算增长率
    private Integer previousUsers;
    private Integer previousMerchants;
    private Integer previousProducts;
    private Integer previousOrders;
    
    // 添加增长率字段
    private Double userGrowthRate;
    private Double merchantGrowthRate;
    private Double productGrowthRate;
    private Double orderGrowthRate;
}
