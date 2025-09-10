package com.example.shop.controller;

import com.example.shop.common.BusinessException;
import com.example.shop.common.Result;
import com.example.shop.dto.AddressCreateRequest;
import com.example.shop.dto.AddressDeleteRequest;
import com.example.shop.dto.AddressUpdateRequest;
import com.example.shop.entity.Address;
import com.example.shop.entity.User;
import com.example.shop.repository.AddressRepository;
import com.example.shop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/address")
public class AddressController {

	@Autowired
	private AddressRepository addressRepository;

	@Autowired
	private UserRepository userRepository;

	@GetMapping("/list")
	public Result<List<Address>> list(Authentication auth) {
		User user = userRepository.findByUsername(auth.getName()).orElseThrow(() -> new BusinessException("用户不存在"));
		return Result.success(addressRepository.findByUser_Id(user.getId()));
	}

	@PostMapping("/create")
	@Transactional
	public Result<Address> create(Authentication auth,
	                             @RequestBody @Validated AddressCreateRequest request) {
		User user = userRepository.findByUsername(auth.getName()).orElseThrow(() -> new BusinessException("用户不存在"));
		Address a = new Address();
		a.setUser(user);
		a.setReceiverName(request.getReceiverName());
		a.setPhone(request.getPhone());
		a.setProvince(request.getProvince());
		a.setCity(request.getCity());
		a.setDistrict(request.getDistrict());
		a.setDetail(request.getDetail());
		boolean makeDefault = request.getIsDefault() != null && request.getIsDefault();
		if (makeDefault) {
			addressRepository.clearDefaultAddress(user.getId());
		}
		a.setIsDefault(makeDefault);
		return Result.success(addressRepository.save(a));
	}

	@PostMapping("/delete")
	public Result<Void> delete(Authentication auth, @RequestBody @Validated AddressDeleteRequest request) {
		User user = userRepository.findByUsername(auth.getName()).orElseThrow(() -> new BusinessException("用户不存在"));
		Address a = addressRepository.findByIdAndUser_Id(request.getId(), user.getId()).orElseThrow(() -> new BusinessException("地址不存在"));
		addressRepository.delete(a);
		return Result.success();
	}

	/** 更新地址 */
	@PutMapping("/update")
	@Transactional
	public Result<Address> update(Authentication auth, @RequestBody @Validated AddressUpdateRequest request) {
		User user = userRepository.findByUsername(auth.getName()).orElseThrow(() -> new BusinessException("用户不存在"));
		Address a = addressRepository.findByIdAndUser_Id(request.getId(), user.getId()).orElseThrow(() -> new BusinessException("地址不存在"));
		a.setReceiverName(request.getReceiverName());
		a.setPhone(request.getPhone());
		a.setProvince(request.getProvince());
		a.setCity(request.getCity());
		a.setDistrict(request.getDistrict());
		a.setDetail(request.getDetail());
		boolean makeDefault = Boolean.TRUE.equals(request.getIsDefault());
		if (makeDefault) {
			addressRepository.clearDefaultAddress(user.getId());
		}
		a.setIsDefault(makeDefault);
		return Result.success(addressRepository.save(a));
	}

	/** 设为默认地址 */
	@PostMapping("/set-default")
	@Transactional
	public Result<Void> setDefault(Authentication auth, @RequestBody @Validated @NotNull(message = "地址ID不能为空") Long id) {
		User user = userRepository.findByUsername(auth.getName()).orElseThrow(() -> new BusinessException("用户不存在"));
		Address a = addressRepository.findByIdAndUser_Id(id, user.getId()).orElseThrow(() -> new BusinessException("地址不存在"));
		addressRepository.clearDefaultAddress(user.getId());
		a.setIsDefault(true);
		addressRepository.save(a);
		return Result.success();
	}
}


