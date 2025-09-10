package com.example.shop.service;

import com.example.shop.common.BusinessException;
import com.example.shop.dto.CartItemAddDTO;
import com.example.shop.entity.CartItem;
import com.example.shop.entity.Product;
import com.example.shop.entity.User;
import com.example.shop.repository.CartItemRepository;
import com.example.shop.repository.ProductRepository;
import com.example.shop.repository.UserRepository;
import com.example.shop.vo.CartSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

	@Autowired
	private CartItemRepository cartItemRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private UserRepository userRepository;

	/**
	 * 添加商品到购物车
	 */
	@Transactional
	public void addItem(String username, CartItemAddDTO dto) {
		User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
		Product product = productRepository.findById(dto.getProductId()).orElseThrow(() -> new BusinessException("商品不存在"));
		
		// 验证商品状态
		if (product.getStatus() != Product.ProductStatus.ON_SALE) {
			throw new BusinessException("商品已下架");
		}
		
		// 验证库存
		if (dto.getQuantity() > product.getStock()) {
			throw new BusinessException("商品库存不足");
		}
		
		CartItem exist = cartItemRepository.findByUserIdAndProductId(user.getId(), product.getId()).orElse(null);
		if (exist == null) {
			CartItem item = new CartItem();
			item.setUser(user);
			item.setProduct(product);
			item.setQuantity(dto.getQuantity());
			item.setChecked(true);
			cartItemRepository.save(item);
		} else {
			// 检查更新后的数量是否超过库存
			int newQuantity = exist.getQuantity() + dto.getQuantity();
			if (newQuantity > product.getStock()) {
				throw new BusinessException("商品库存不足");
			}
			exist.setQuantity(newQuantity);
			cartItemRepository.save(exist);
		}
	}

	/**
	 * 查看购物车列表
	 */
	public List<CartItem> list(String username) {
		User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
		return cartItemRepository.findByUserId(user.getId());
	}

	/**
	 * 更新购物车商品数量
	 */
	@Transactional
	public void updateQuantity(String username, Long cartItemId, Integer quantity) {
		User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
		CartItem cartItem = cartItemRepository.findById(cartItemId)
			.orElseThrow(() -> new BusinessException("购物车商品不存在"));
		
		// 验证是否属于当前用户
		if (!cartItem.getUser().getId().equals(user.getId())) {
			throw new BusinessException("无权操作此购物车商品");
		}
		
		// 验证数量
		if (quantity <= 0) {
			throw new BusinessException("商品数量必须大于0");
		}
		
		// 验证库存
		if (quantity > cartItem.getProduct().getStock()) {
			throw new BusinessException("商品库存不足");
		}
		
		cartItem.setQuantity(quantity);
		cartItemRepository.save(cartItem);
	}

	/**
	 * 删除购物车商品
	 */
	@Transactional
	public void removeItem(String username, Long cartItemId) {
		User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
		CartItem cartItem = cartItemRepository.findById(cartItemId)
			.orElseThrow(() -> new BusinessException("购物车商品不存在"));
		
		// 验证是否属于当前用户
		if (!cartItem.getUser().getId().equals(user.getId())) {
			throw new BusinessException("无权操作此购物车商品");
		}
		
		cartItemRepository.delete(cartItem);
	}

	/**
	 * 批量删除购物车商品
	 */
	@Transactional
	public void removeItems(String username, List<Long> cartItemIds) {
		User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
		cartItemRepository.deleteByUserIdAndCartItemIds(user.getId(), cartItemIds);
	}

	/**
	 * 切换商品选择状态
	 */
	@Transactional
	public void toggleChecked(String username, Long cartItemId) {
		User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
		CartItem cartItem = cartItemRepository.findById(cartItemId)
			.orElseThrow(() -> new BusinessException("购物车商品不存在"));
		
		// 验证是否属于当前用户
		if (!cartItem.getUser().getId().equals(user.getId())) {
			throw new BusinessException("无权操作此购物车商品");
		}
		
		cartItem.setChecked(!cartItem.getChecked());
		cartItemRepository.save(cartItem);
	}

	/**
	 * 获取购物车统计信息
	 */
	public CartSummary getSummary(String username) {
		User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
		List<CartItem> items = cartItemRepository.findByUserId(user.getId());
		
		int totalItems = 0;
		int checkedItems = 0;
		BigDecimal totalAmount = BigDecimal.ZERO;
		
		for (CartItem item : items) {
			totalItems += item.getQuantity();
			if (item.getChecked()) {
				checkedItems += item.getQuantity();
				totalAmount = totalAmount.add(
					item.getProduct().getPrice().multiply(new BigDecimal(item.getQuantity()))
				);
			}
		}
		
		return new CartSummary(totalItems, checkedItems, totalAmount);
	}

	/**
	 * 获取已选择的商品（用于结算）
	 */
	public List<CartItem> getCheckedItems(String username) {
		User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
		return cartItemRepository.findCheckedItemsByUserId(user.getId());
	}

	/**
	 * 清空购物车
	 */
	@Transactional
	public void clearCart(String username) {
		User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
		List<CartItem> items = cartItemRepository.findByUserId(user.getId());
		cartItemRepository.deleteAll(items);
	}
}


