package com.example.shop.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AddressDeleteRequest {

	@NotNull(message = "地址ID不能为空")
	private Long id;
}
