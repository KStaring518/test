package com.example.shop.service;

import com.example.shop.common.BusinessException;
import com.example.shop.dto.UserLoginDTO;
import com.example.shop.dto.UserRegisterDTO;
import com.example.shop.entity.User;
import com.example.shop.repository.UserRepository;
import com.example.shop.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtUtil jwtUtil;

	public void register(UserRegisterDTO dto) {
		if (!dto.getPassword().equals(dto.getConfirmPassword())) {
			throw new BusinessException(400, "两次输入的密码不一致");
		}
		if (userRepository.existsByUsername(dto.getUsername())) {
			throw new BusinessException(400, "用户名已存在");
		}
		User user = new User();
		user.setUsername(dto.getUsername());
		user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
		user.setNickname(dto.getNickname());
		user.setPhone(dto.getPhone());
		user.setEmail(dto.getEmail());
		user.setRole(User.UserRole.USER);
		userRepository.save(user);
	}

	public String login(UserLoginDTO dto) {
		User user = userRepository.findByUsername(dto.getUsername())
				.orElseThrow(() -> new BusinessException(400, "用户不存在"));
		if (user.getStatus() == User.UserStatus.DISABLED) {
			throw new BusinessException(403, "账户已被禁用，请联系管理员");
		}
		if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
			throw new BusinessException(400, "用户名或密码错误");
		}
		return jwtUtil.generateToken(user.getUsername(), user.getRole().name());
	}
}


