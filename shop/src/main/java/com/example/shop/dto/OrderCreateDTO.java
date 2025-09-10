package com.example.shop.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class OrderCreateDTO {

	@NotEmpty(message = "购物车项不能为空")
	private List<Long> cartItemIds;

	@NotNull(message = "收货地址不能为空")
	private Long addressId;

	private String remark;
}


