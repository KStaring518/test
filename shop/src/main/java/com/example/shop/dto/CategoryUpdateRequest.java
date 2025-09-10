package com.example.shop.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CategoryUpdateRequest {
    
    @NotBlank(message = "分类名称不能为空")
    private String name;
    
    private Long parentId;
    
    private Integer sortOrder = 0;
}
