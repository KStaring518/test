package com.example.shop.dto;

import com.example.shop.entity.Order;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminOrderListDTO {
    private Long id;
    private String orderNo;
    private Order.OrderStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private String buyerName;      // 用户昵称或用户名
    private String merchantName;   // 商家店铺名

    public static AdminOrderListDTO fromOrder(Order order) {
        AdminOrderListDTO dto = new AdminOrderListDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setCreatedAt(order.getCreatedAt());
        if (order.getUser() != null) {
            String nickname = order.getUser().getNickname();
            dto.setBuyerName(nickname != null && !nickname.isEmpty() ? nickname : order.getUser().getUsername());
        }
        if (order.getMerchant() != null) {
            dto.setMerchantName(order.getMerchant().getShopName());
        }
        return dto;
    }
}


