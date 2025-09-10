package com.example.shop.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {
	private Long id;
	private String username;
	private String nickname;
	private String phone;
	private String email;
	private String avatarUrl;
	private String role;
	private String status;
	private LocalDateTime createdAt;
}


