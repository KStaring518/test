package com.example.shop.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemVO {
	private Long id;
	private Long productId;
	private String productName;
	private String coverImage;
	private BigDecimal price;
	private Integer quantity;
	private BigDecimal subtotal;
}


