package com.example.shop.dto;

import lombok.Data;

import javax.validation.constraints.*;

@Data
public class UserRegisterDTO {

	@NotBlank(message = "用户名不能为空")
	@Size(min = 4, max = 20, message = "用户名长度必须在4-20个字符之间")
	@Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
	private String username;

	@NotBlank(message = "密码不能为空")
	@Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
	private String password;

	@NotBlank(message = "确认密码不能为空")
	private String confirmPassword;

	@NotBlank(message = "昵称不能为空")
	@Size(max = 50, message = "昵称长度不能超过50个字符")
	private String nickname;

	@NotBlank(message = "手机号不能为空")
	@Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
	private String phone;

	@NotBlank(message = "邮箱不能为空")
	@Email(message = "邮箱格式不正确")
	private String email;
}


