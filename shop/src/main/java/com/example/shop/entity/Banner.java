package com.example.shop.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "banners")
public class Banner {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 100)
	private String title;

	@Column(name = "image_url", nullable = false, length = 255)
	private String imageUrl;

	@Column(name = "link_url", length = 255)
	private String linkUrl;

	@Column(name = "sort_order")
	private Integer sortOrder = 0;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private BannerStatus status = BannerStatus.ENABLED;

	@CreationTimestamp
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	public enum BannerStatus {
		ENABLED, DISABLED
	}
}


