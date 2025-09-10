package com.example.shop.controller;

import com.example.shop.common.PageResult;
import com.example.shop.common.Result;
import com.example.shop.dto.OrderCreateDTO;
import com.example.shop.dto.OrderDetailDTO;
import com.example.shop.entity.Order;
import com.example.shop.entity.OrderItem;
import com.example.shop.entity.Shipment;
import com.example.shop.repository.ReviewRepository;
import com.example.shop.service.OrderService;
import com.example.shop.vo.OrderSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {

	@Autowired
	private OrderService orderService;
	
	@Autowired
	private ReviewRepository reviewRepository;

	@PostMapping("/create")
	public Result<Order> create(Authentication auth, @Validated @RequestBody OrderCreateDTO dto) {
		return Result.success(orderService.createOrder(auth.getName(), dto));
	}

	@PostMapping("/pay/mock")
	public Result<Order> mockPay(@RequestParam String orderNo) {
		return Result.success(orderService.mockPay(orderNo));
	}

	@PostMapping("/ship")
	public Result<Shipment> ship(@RequestParam String orderNo) {
		return Result.success(orderService.ship(orderNo));
	}

	@GetMapping("/my")
	public Result<PageResult<Order>> my(Authentication auth,
	                                   @RequestParam(defaultValue = "1") Integer page,
	                                   @RequestParam(defaultValue = "10") Integer size,
	                                   @RequestParam(required = false) Order.OrderStatus status) {
		Page<Order> p = orderService.listMyOrders(auth.getName(), page, size, status);
		return Result.success(new PageResult<>(p.getTotalElements(), p.getTotalPages(), page, size, p.getContent()));
	}

	/**
	 * 获取订单详情
	 */
	@GetMapping("/{orderId}")
	public Result<OrderDetailDTO> getDetail(Authentication auth, @PathVariable Long orderId) {
		// 根据用户角色调用不同的服务方法
		Order order = orderService.getOrderDetail(auth.getName(), orderId);
		
		// 构建评价状态映射（使用OrderService中已经计算好的状态）
		Map<Long, Boolean> reviewStatusMap = new HashMap<>();
		if (order.getOrderItems() != null) {
			for (OrderItem item : order.getOrderItems()) {
				// 使用OrderService中已经计算好的评价状态
				boolean hasReview = reviewRepository.countByOrderItem_Id(item.getId()) > 0;
				reviewStatusMap.put(item.getId(), hasReview);
			}
		}
		
		return Result.success(OrderDetailDTO.fromOrder(order, reviewStatusMap));
	}

	/**
	 * 取消订单
	 */
	@PostMapping("/{orderId}/cancel")
	public Result<Void> cancel(Authentication auth, @PathVariable Long orderId) {
		orderService.cancelOrder(auth.getName(), orderId);
		return Result.success();
	}

	/**
	 * 确认收货
	 */
	@PostMapping("/{orderId}/confirm")
	public Result<Void> confirmReceive(Authentication auth, @PathVariable Long orderId) {
		orderService.confirmReceive(auth.getName(), orderId);
		return Result.success();
	}

	/**
	 * 获取订单统计
	 */
	@GetMapping("/summary")
	public Result<OrderSummary> getSummary(Authentication auth) {
		return Result.success(orderService.getOrderSummary(auth.getName()));
	}
}


