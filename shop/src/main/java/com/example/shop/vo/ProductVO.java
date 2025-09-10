package com.example.shop.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductVO {
	private Long id;
	private String name;
	private String subtitle;
	private Long categoryId;
	private String categoryName;
	private String coverImage;
	private BigDecimal price;
	private String unit;
	private Integer stock;
	private String status;
	private String statusText;
	private String description;
	private LocalDateTime createdAt;

	public String getStockStatus() {
		if (stock == null) return "未知";
		if (stock <= 0) return "缺货";
		if (stock < 10) return "库存紧张";
		return "有货";
	}
}


