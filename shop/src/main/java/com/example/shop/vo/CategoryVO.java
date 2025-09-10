package com.example.shop.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CategoryVO {
	private Long id;
	private String name;
	private Long parentId;
	private String parentName;
	private Integer sortOrder;
	private String status;
	private LocalDateTime createdAt;
	private List<CategoryVO> children;
}


