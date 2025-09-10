package com.example.shop.controller;

import com.example.shop.common.Result;
import com.example.shop.dto.CartItemAddDTO;
import com.example.shop.entity.CartItem;
import com.example.shop.service.CartService;
import com.example.shop.vo.CartSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

	@Autowired
	private CartService cartService;

	/**
	 * 添加商品到购物车
	 */
	@PostMapping("/add")
	public Result<Void> add(Authentication auth, @Validated @RequestBody CartItemAddDTO dto) {
		cartService.addItem(auth.getName(), dto);
		return Result.success();
	}

	/**
	 * 查看购物车列表
	 */
	@GetMapping("/list")
	public Result<List<CartItem>> list(Authentication auth) {
		return Result.success(cartService.list(auth.getName()));
	}

	/**
	 * 更新购物车商品数量
	 */
	@PutMapping("/{cartItemId}/quantity")
	public Result<Void> updateQuantity(Authentication auth, 
	                                  @PathVariable Long cartItemId,
	                                  @RequestParam Integer quantity) {
		cartService.updateQuantity(auth.getName(), cartItemId, quantity);
		return Result.success();
	}

	/**
	 * 删除购物车商品
	 */
	@DeleteMapping("/{cartItemId}")
	public Result<Void> removeItem(Authentication auth, @PathVariable Long cartItemId) {
		cartService.removeItem(auth.getName(), cartItemId);
		return Result.success();
	}

	/**
	 * 批量删除购物车商品
	 */
	@DeleteMapping("/batch")
	public Result<Void> removeItems(Authentication auth, @RequestBody List<Long> cartItemIds) {
		cartService.removeItems(auth.getName(), cartItemIds);
		return Result.success();
	}

	/**
	 * 切换商品选择状态
	 */
	@PutMapping("/{cartItemId}/toggle")
	public Result<Void> toggleChecked(Authentication auth, @PathVariable Long cartItemId) {
		cartService.toggleChecked(auth.getName(), cartItemId);
		return Result.success();
	}

	/**
	 * 获取购物车统计信息
	 */
	@GetMapping("/summary")
	public Result<CartSummary> getSummary(Authentication auth) {
		return Result.success(cartService.getSummary(auth.getName()));
	}

	/**
	 * 获取已选择的商品（用于结算）
	 */
	@GetMapping("/checked")
	public Result<List<CartItem>> getCheckedItems(Authentication auth) {
		return Result.success(cartService.getCheckedItems(auth.getName()));
	}

	/**
	 * 清空购物车
	 */
	@DeleteMapping("/clear")
	public Result<Void> clearCart(Authentication auth) {
		cartService.clearCart(auth.getName());
		return Result.success();
	}
}


