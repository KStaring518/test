package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartSummary {
    private int totalItems;
    private int checkedItems;
    private BigDecimal totalAmount;
}
