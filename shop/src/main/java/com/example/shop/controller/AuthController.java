package com.example.shop.controller;

import com.example.shop.common.BusinessException;
import com.example.shop.common.Result;
import com.example.shop.dto.UserLoginDTO;
import com.example.shop.dto.UserRegisterDTO;
import com.example.shop.entity.User;
import com.example.shop.repository.UserRepository;
import com.example.shop.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import com.example.shop.dto.MerchantRegisterRequest;
import com.example.shop.entity.Merchant;
import com.example.shop.repository.MerchantRepository;

@RestController
@RequestMapping("/auth")
public class AuthController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private MerchantRepository merchantRepository;

	@PostMapping("/register")
	public Result<Void> register(@Validated @RequestBody UserRegisterDTO dto) {
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
		return Result.success();
	}

	@PostMapping("/login")
	public Result<Map<String, Object>> login(@Validated @RequestBody UserLoginDTO dto) {
		User user = userRepository.findByUsername(dto.getUsername())
				.orElseThrow(() -> new BusinessException(400, "用户不存在"));
		// 禁用用户不可登录
		if (user.getStatus() == User.UserStatus.DISABLED) {
			throw new BusinessException(403, "账户已被禁用，请联系管理员");
		}
		if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
			throw new BusinessException(400, "用户名或密码错误");
		}
		String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
		Map<String, Object> data = new HashMap<>();
		data.put("token", token);
		// 返回完整用户信息，前端可直接读取 role 等字段
		data.put("user", user);
		// 兼容旧前端：同时返回 role 字段
		data.put("role", user.getRole().name());
		return Result.success(data);
	}

	@PostMapping("/merchant/register")
	public Result<Void> registerMerchant(@Validated @RequestBody MerchantRegisterRequest dto, Authentication auth) {
		String username = auth.getName();
		User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
		
		// 检查用户是否已经是商家
		if (user.getRole() == User.UserRole.MERCHANT) {
			throw new BusinessException("您已经是商家了");
		}
		
		// 检查是否已经申请过
		if (merchantRepository.existsByUser(user)) {
			throw new BusinessException("您已经申请过商家了，请等待审核");
		}
		
		// 创建商家申请
		Merchant merchant = new Merchant();
		merchant.setUser(user);
		merchant.setShopName(dto.getShopName());
		merchant.setShopDescription(dto.getShopDescription());
		merchant.setContactPhone(dto.getContactPhone());
		merchant.setBusinessLicense(dto.getBusinessLicense());
		merchant.setStatus(Merchant.MerchantStatus.PENDING);
		
		merchantRepository.save(merchant);
		
		return Result.success();
	}

	@GetMapping("/users")
	public Result<List<User>> getAllUsers() {
		List<User> users = userRepository.findAll();
		return Result.success(users);
	}
}


