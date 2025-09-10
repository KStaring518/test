package com.example.shop.dto;

import com.example.shop.entity.Order;
import com.example.shop.entity.OrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class OrderDetailDTO {
    private Long id;
    private String orderNo;
    private String status;
    private BigDecimal totalAmount;
    private String payType;
    private LocalDateTime payTime;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String remark;
    private LocalDateTime createdAt;
    private List<OrderItemDTO> orderItems;
    private ShipmentDTO shipment;
    private Long userId;

    public static OrderDetailDTO fromOrder(Order order, Map<Long, Boolean> reviewStatusMap) {
        OrderDetailDTO dto = new OrderDetailDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setStatus(order.getStatus().name());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setPayType(order.getPayType());
        dto.setPayTime(order.getPayTime());
        dto.setReceiverName(order.getReceiverName());
        dto.setReceiverPhone(order.getReceiverPhone());
        dto.setReceiverAddress(order.getReceiverAddress());
        dto.setRemark(order.getRemark());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUserId(order.getUser() != null ? order.getUser().getId() : null);
        
        if (order.getOrderItems() != null) {
            dto.setOrderItems(order.getOrderItems().stream()
                    .map(item -> {
                        boolean hasReview = reviewStatusMap.getOrDefault(item.getId(), false);
                        return OrderItemDTO.fromOrderItem(item, hasReview);
                    })
                    .collect(Collectors.toList()));
        }
        
        if (order.getShipment() != null) {
            dto.setShipment(ShipmentDTO.fromShipment(order.getShipment()));
        }
        
        return dto;
    }

    @Data
    public static class OrderItemDTO {
        private Long id;
        private ProductDTO product;
        private String productNameSnapshot;
        private BigDecimal priceSnapshot;
        private Integer quantity;
        private BigDecimal subtotal;
        private Boolean hasReview;

        public static OrderItemDTO fromOrderItem(OrderItem orderItem, boolean hasReview) {
            OrderItemDTO dto = new OrderItemDTO();
            dto.setId(orderItem.getId());
            dto.setProduct(ProductDTO.fromProduct(orderItem.getProduct()));
            dto.setProductNameSnapshot(orderItem.getProductNameSnapshot());
            dto.setPriceSnapshot(orderItem.getPriceSnapshot());
            dto.setQuantity(orderItem.getQuantity());
            dto.setSubtotal(orderItem.getSubtotal());
            dto.setHasReview(hasReview);
            return dto;
        }
    }

    @Data
    public static class ProductDTO {
        private Long id;
        private String name;
        private String subtitle;
        private String coverImage;
        private BigDecimal price;
        private String unit;

        public static ProductDTO fromProduct(com.example.shop.entity.Product product) {
            ProductDTO dto = new ProductDTO();
            dto.setId(product.getId());
            dto.setName(product.getName());
            dto.setSubtitle(product.getSubtitle());
            dto.setCoverImage(product.getCoverImage());
            dto.setPrice(product.getPrice());
            dto.setUnit(product.getUnit());
            return dto;
        }
    }
    
    @Data
    public static class ShipmentDTO {
        private Long id;
        private String shipmentNo;
        private String carrier;
        private String trackingNo;
        private String status;
        private LocalDateTime shippedAt;
        private String shippingAddress;
        
        public static ShipmentDTO fromShipment(com.example.shop.entity.Shipment shipment) {
            ShipmentDTO dto = new ShipmentDTO();
            dto.setId(shipment.getId());
            dto.setShipmentNo(shipment.getShipmentNo());
            dto.setCarrier(shipment.getCarrier());
            dto.setTrackingNo(shipment.getTrackingNo());
            dto.setStatus(shipment.getStatus().name());
            dto.setShippedAt(shipment.getShippedAt());
            dto.setShippingAddress(shipment.getShippingAddress());
            return dto;
        }
    }
}
