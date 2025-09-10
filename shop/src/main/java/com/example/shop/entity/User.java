package com.example.shop.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false, length = 50)
	private String username;

	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Column(length = 50)
	private String nickname;

	@Column(length = 20)
	private String phone;

	@Column(length = 100)
	private String email;

	@Column(name = "avatar_url", length = 255)
	private String avatarUrl;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private UserRole role = UserRole.USER;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private UserStatus status = UserStatus.ENABLED;

	@CreationTimestamp
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	public enum UserRole {
		USER, MERCHANT, ADMIN
	}

	public enum UserStatus {
		ENABLED, DISABLED
	}
}


