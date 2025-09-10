package com.example.shop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "shipments")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "shipment_no", unique = true, nullable = false, length = 50)
    private String shipmentNo;

    @Column(length = 50)
    private String carrier;

    @Column(name = "tracking_no", length = 100)
    private String trackingNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShipmentStatus status = ShipmentStatus.SHIPPED;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "estimated_delivery")
    private LocalDateTime estimatedDelivery;

    @Column(name = "actual_delivery")
    private LocalDateTime actualDelivery;

    @Column(name = "shipping_address", length = 500)
    private String shippingAddress;

    // 发货人信息（商家侧）
    @Column(name = "sender_name", length = 100)
    private String senderName;

    @Column(name = "sender_phone", length = 30)
    private String senderPhone;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum ShipmentStatus {
        SHIPPED, IN_TRANSIT, DELIVERED, RECEIVED
    }
}


