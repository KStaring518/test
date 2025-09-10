package com.example.shop.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "addresses")
public class Address {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "receiver_name", nullable = false, length = 50)
	private String receiverName;

	@Column(nullable = false, length = 20)
	private String phone;

	@Column(nullable = false, length = 50)
	private String province;

	@Column(nullable = false, length = 50)
	private String city;

	@Column(nullable = false, length = 50)
	private String district;

	@Column(nullable = false, length = 200)
	private String detail;

	@Column(name = "is_default", nullable = false)
	private Boolean isDefault = false;

	@CreationTimestamp
	@Column(name = "created_at")
	private LocalDateTime createdAt;
}


