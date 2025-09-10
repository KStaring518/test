package com.example.shop.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import java.math.BigDecimal;

@Data
public class ProductCreateRequest {
    
    @NotBlank(message = "商品名称不能为空")
    private String name;
    
    private String subtitle;
    
    @NotNull(message = "商品分类不能为空")
    private Long categoryId;
    
    private String coverImage;
    
    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    private BigDecimal price;
    
    private String unit;
    
    @NotNull(message = "商品库存不能为空")
    @Min(value = 0, message = "商品库存不能小于0")
    private Integer stock;
    
    private String description;
}
