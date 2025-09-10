package com.example.shop.dto;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class MerchantUpdateProfileRequest {
    @Size(max = 100)
    private String shopName;

    @Size(max = 500)
    private String shopDescription;

    @Size(max = 20)
    private String contactPhone;

    @Size(max = 255)
    private String businessLicense;
}


