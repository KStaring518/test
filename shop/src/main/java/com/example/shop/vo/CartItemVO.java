package com.example.shop.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CartItemVO {
	private Long id;
	private Long productId;
	private String productName;
	private String coverImage;
	private BigDecimal price;
	private String unit;
	private Integer stock;
	private Integer quantity;
	private Boolean checked;
	private LocalDateTime createdAt;

	public BigDecimal getSubtotal() {
		if (price == null || quantity == null) return BigDecimal.ZERO;
		return price.multiply(new BigDecimal(quantity));
	}
}


