package com.example.shop.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class ShipmentRequest {
    @NotBlank(message = "物流公司不能为空")
    private String carrier;

    @NotBlank(message = "物流单号不能为空")
    private String trackingNo;

    // 发货人/发货地址信息
    @NotBlank(message = "发货人不能为空")
    private String senderName;

    private String senderPhone;

    @NotBlank(message = "发货地址不能为空")
    private String senderAddress;
}
