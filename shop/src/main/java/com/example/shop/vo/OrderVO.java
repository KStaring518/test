package com.example.shop.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {
	private Long id;
	private String orderNo;
	private String status;
	private String statusText;
	private BigDecimal totalAmount;
	private String payType;
	private LocalDateTime payTime;
	private String receiverName;
	private String receiverPhone;
	private String receiverAddress;
	private String remark;
	private LocalDateTime createdAt;
	private List<OrderItemVO> orderItems;
}


