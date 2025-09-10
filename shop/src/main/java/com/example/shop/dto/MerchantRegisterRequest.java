package com.example.shop.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class MerchantRegisterRequest {
    
    @NotBlank(message = "店铺名称不能为空")
    @Size(max = 100, message = "店铺名称长度不能超过100个字符")
    private String shopName;
    
    @Size(max = 500, message = "店铺描述长度不能超过500个字符")
    private String shopDescription;
    
    @NotBlank(message = "联系电话不能为空")
    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    private String contactPhone;
    
    @Size(max = 255, message = "营业执照长度不能超过255个字符")
    private String businessLicense;
}
