package com.example.shop.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "order_items")
public class OrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	@JsonBackReference
	private Order order;

	@ManyToOne
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(name = "product_name_snapshot", nullable = false, length = 100)
	private String productNameSnapshot;

	@Column(name = "price_snapshot", nullable = false, precision = 10, scale = 2)
	private BigDecimal priceSnapshot;

	@Column(nullable = false)
	private Integer quantity;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal subtotal;

	@OneToOne(mappedBy = "orderItem", fetch = FetchType.LAZY)
	@JsonIgnore
	private Review review;
	
	// 添加评价状态字段，不映射到数据库
	@Transient
	private Boolean hasReview;
}


