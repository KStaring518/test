package com.example.shop.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class CartItemAddDTO {

	@NotNull(message = "商品ID不能为空")
	private Long productId;

	@NotNull(message = "商品数量不能为空")
	@Min(value = 1, message = "商品数量必须大于0")
	private Integer quantity;
}


