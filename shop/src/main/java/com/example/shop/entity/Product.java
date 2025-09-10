package com.example.shop.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "products")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(length = 200)
	private String subtitle;

	@ManyToOne
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	@ManyToOne
	@JoinColumn(name = "merchant_id", nullable = true) // 临时允许null，避免数据问题
	private Merchant merchant;

	@Column(name = "cover_image", length = 255)
	private String coverImage;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	@Column(length = 20)
	private String unit;

	@Column(nullable = false)
	private Integer stock;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ProductStatus status = ProductStatus.ON_SALE;

	@Column(columnDefinition = "TEXT")
	private String description;

	@CreationTimestamp
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	public enum ProductStatus {
		ON_SALE, OFF_SALE, SOLD_OUT
	}
}


