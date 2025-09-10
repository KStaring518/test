package com.example.shop.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "merchants")
public class Merchant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;

	@Column(name = "shop_name", nullable = false, length = 100)
	private String shopName;

	@Column(name = "shop_description", length = 500)
	private String shopDescription;

	@Column(name = "contact_phone", length = 20)
	private String contactPhone;

	@Column(name = "business_license", length = 255)
	private String businessLicense;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private MerchantStatus status = MerchantStatus.PENDING;

	@CreationTimestamp
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	public enum MerchantStatus {
		PENDING, APPROVED, REJECTED, SUSPENDED
	}
}


