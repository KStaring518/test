package com.example.shop.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "orders")
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "order_no", unique = true, nullable = false, length = 50)
	private String orderNo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	@JsonIgnore
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "merchant_id", nullable = false)
	@NotFound(action = NotFoundAction.IGNORE)
	@JsonIgnore
	private Merchant merchant;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OrderStatus status = OrderStatus.UNPAID;

	@Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal totalAmount;

	@Column(name = "pay_type", length = 20)
	private String payType;

	@Column(name = "pay_time")
	private LocalDateTime payTime;

	@Column(name = "receiver_name", length = 50)
	private String receiverName;

	@Column(name = "receiver_phone", length = 20)
	private String receiverPhone;

	@Column(name = "receiver_address", length = 500)
	private String receiverAddress;

	@Column(length = 200)
	private String remark;

	@CreationTimestamp
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonManagedReference
	private List<OrderItem> orderItems;
	
	@OneToOne(mappedBy = "order", fetch = FetchType.LAZY)
	private Shipment shipment;

	public enum OrderStatus {
		UNPAID, PAID, SHIPPED, FINISHED, CLOSED
	}
}


