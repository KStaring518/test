package com.example.shop.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class CategoryCreateDTO {

	@NotBlank(message = "分类名称不能为空")
	@Size(max = 50, message = "分类名称长度不能超过50个字符")
	private String name;

	private Long parentId;

	@NotNull(message = "排序不能为空")
	private Integer sortOrder = 0;
}


