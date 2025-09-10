package com.example.shop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reviews")
public class Review {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "order_item_id", nullable = false, unique = true)
	@JsonIgnore
	private OrderItem orderItem;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private Integer rating;

	@Column(columnDefinition = "TEXT")
	private String content;

	@Column(columnDefinition = "JSON")
	private String images;

	@Column(name = "is_anonymous")
	private Boolean isAnonymous = false;

	@CreationTimestamp
	@Column(name = "created_at")
	private LocalDateTime createdAt;
}


