package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantStatistics {
    private Integer totalProducts;
    private Integer onSaleProducts;
    private Integer totalOrders;
    private Integer pendingOrders;
    private Integer shippedOrders;
    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
}
