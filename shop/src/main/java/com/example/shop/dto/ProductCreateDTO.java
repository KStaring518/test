package com.example.shop.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class ProductCreateDTO {

	@NotBlank(message = "商品名称不能为空")
	@Size(max = 100, message = "商品名称长度不能超过100个字符")
	private String name;

	@Size(max = 200, message = "商品副标题长度不能超过200个字符")
	private String subtitle;

	@NotNull(message = "商品分类不能为空")
	private Long categoryId;

	@NotBlank(message = "商品主图不能为空")
	private String coverImage;

	@NotNull(message = "商品价格不能为空")
	@DecimalMin(value = "0.01", message = "商品价格必须大于0")
	@DecimalMax(value = "999999.99", message = "商品价格不能超过999999.99")
	private BigDecimal price;

	@Size(max = 20, message = "商品单位长度不能超过20个字符")
	private String unit;

	@NotNull(message = "商品库存不能为空")
	@Min(value = 0, message = "商品库存不能小于0")
	@Max(value = 999999, message = "商品库存不能超过999999")
	private Integer stock;

	@Size(max = 1000, message = "商品描述长度不能超过1000个字符")
	private String description;
}


