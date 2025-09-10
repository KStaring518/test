package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummary {
    private int totalOrders;
    private int unpaidOrders;
    private int paidOrders;
    private int shippedOrders;
    private int finishedOrders;
    private int cancelledOrders;
}
