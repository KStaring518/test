package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfig {
    private String siteName;
    private String contactPhone;
    private String contactEmail;
    private Integer orderAutoCloseMinutes; // 未支付自动关闭（分钟）
    private Integer shipmentAutoConfirmDays; // 发货后自动确认收货（天）
}


