package com.example.shop.service;

import com.example.shop.common.BusinessException;
import com.example.shop.dto.OrderCreateDTO;
import com.example.shop.entity.*;
import com.example.shop.entity.Order.OrderStatus;
import com.example.shop.repository.*;
import com.example.shop.vo.OrderSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AddressRepository addressRepository;

	@Autowired
	private CartItemRepository cartItemRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private OrderItemRepository orderItemRepository;

	@Autowired
	private ShipmentRepository shipmentRepository;
	
	@Autowired
	private ReviewRepository reviewRepository;

	@Autowired
	private LogisticsTrackRepository logisticsTrackRepository;
	
	@Autowired
	private MerchantRepository merchantRepository;

	@Transactional
	public Order createOrder(String username, OrderCreateDTO dto) {
		User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
		Address address = addressRepository.findByIdAndUser_Id(dto.getAddressId(), user.getId()).orElseThrow(() -> new BusinessException("地址不存在"));
		
		// 获取已选择的购物车商品
		List<CartItem> items = cartItemRepository.findCheckedItemsByUserId(user.getId()).stream()
				.filter(i -> dto.getCartItemIds().contains(i.getId()))
				.collect(Collectors.toList());
		
		if (items.isEmpty()) {
			throw new BusinessException("请选择要购买的商品");
		}

		// 验证库存
		for (CartItem item : items) {
			Product product = item.getProduct();
			if (product.getStatus() != Product.ProductStatus.ON_SALE) {
				throw new BusinessException("商品 " + product.getName() + " 已下架");
			}
			if (product.getStock() < item.getQuantity()) {
				throw new BusinessException("商品 " + product.getName() + " 库存不足");
			}
		}

		// 先计算总金额
		BigDecimal total = BigDecimal.ZERO;
		for (CartItem item : items) {
			Product p = item.getProduct();
			total = total.add(p.getPrice().multiply(new BigDecimal(item.getQuantity())));
		}

		Order order = new Order();
		order.setOrderNo("O" + UUID.randomUUID().toString().replace("-", "").substring(0, 18));
		order.setUser(user);
		// 设置商家信息（假设所有商品都是同一个商家的）
		order.setMerchant(items.get(0).getProduct().getMerchant());
		order.setStatus(Order.OrderStatus.UNPAID);
		order.setTotalAmount(total);
		order.setReceiverName(address.getReceiverName());
		order.setReceiverPhone(address.getPhone());
		order.setReceiverAddress(address.getProvince() + " " + address.getCity() + " " + address.getDistrict() + " " + address.getDetail());
		order.setRemark(dto.getRemark());

		Order saved = orderRepository.save(order);
		
		// 创建订单项
		for (CartItem item : items) {
			Product p = item.getProduct();
			OrderItem oi = new OrderItem();
			oi.setOrder(saved);
			oi.setProduct(p);
			oi.setProductNameSnapshot(p.getName());
			oi.setPriceSnapshot(p.getPrice());
			oi.setQuantity(item.getQuantity());
			oi.setSubtotal(p.getPrice().multiply(new BigDecimal(item.getQuantity())));
			orderItemRepository.save(oi);
		}
		
		// 购物车结算后清理所选项
		cartItemRepository.deleteByUserIdAndCartItemIds(user.getId(), dto.getCartItemIds());
		
		return saved;
	}

	@Transactional
	public Order mockPay(String orderNo) {
		Order order = orderRepository.findByOrderNo(orderNo).orElseThrow(() -> new BusinessException("订单不存在"));
		if (order.getStatus() != Order.OrderStatus.UNPAID) throw new BusinessException("订单状态不正确");
		// 扣减库存（支付成功后）
		List<OrderItem> orderItems = orderItemRepository.findByOrder_Id(order.getId());
		for (OrderItem oi : orderItems) {
			Product p = oi.getProduct();
			if (p.getStock() == null || p.getStock() < oi.getQuantity()) {
				throw new BusinessException(400, "库存不足: " + p.getName());
			}
			p.setStock(p.getStock() - oi.getQuantity());
			productRepository.save(p);
		}
		order.setStatus(Order.OrderStatus.PAID);
		order.setPayType("MOCK");
		order.setPayTime(LocalDateTime.now());
		return orderRepository.save(order);
	}

	@Transactional
	public Shipment ship(String orderNo) {
		Order order = orderRepository.findByOrderNo(orderNo).orElseThrow(() -> new BusinessException("订单不存在"));
		if (order.getStatus() != Order.OrderStatus.PAID) throw new BusinessException("只有已支付订单可发货");
		Shipment shipment = new Shipment();
		shipment.setOrder(order);
		shipment.setShipmentNo("S" + UUID.randomUUID().toString().replace("-", "").substring(0, 18));
		shipment.setStatus(Shipment.ShipmentStatus.SHIPPED);
		shipment.setShippedAt(LocalDateTime.now());
		shipment.setShippingAddress(order.getReceiverAddress());
		Shipment saved = shipmentRepository.save(shipment);

		// 模拟第一条物流轨迹
		LogisticsTrack t = new LogisticsTrack();
		t.setShipment(saved);
		t.setLocation("商家仓库");
		t.setDescription("包裹已出库");
		t.setTrackTime(LocalDateTime.now());
		logisticsTrackRepository.save(t);

		order.setStatus(Order.OrderStatus.SHIPPED);
		orderRepository.save(order);
		return saved;
	}

	public Page<Order> listMyOrders(String username, Integer page, Integer size, Order.OrderStatus status) {
		try {
			System.out.println("开始查询用户订单 - 用户名: " + username + ", 页码: " + page + ", 大小: " + size + ", 状态: " + status);
			
			User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
			System.out.println("找到用户: " + user.getId() + ", 角色: " + user.getRole());
			
			PageRequest pr = PageRequest.of(page - 1, size);
			Page<Order> orders = (status == null)
					? orderRepository.findByUserId(user.getId(), pr)
					: orderRepository.findByUserIdAndStatus(user.getId(), status, pr);
			
			System.out.println("查询到订单数量: " + orders.getTotalElements());
			
			// 为每个订单加载订单项
			for (Order order : orders.getContent()) {
				List<OrderItem> orderItems = orderItemRepository.findByOrder_IdWithProduct(order.getId());
				order.setOrderItems(orderItems);
				System.out.println("订单 " + order.getId() + " 加载了 " + orderItems.size() + " 个订单项");
				
				// 为每个订单项设置评价状态
				for (OrderItem item : orderItems) {
					try {
						Long reviewCount = reviewRepository.countByOrderItem_Id(item.getId());
						boolean hasReview = reviewCount > 0;
						// 使用反射设置hasReview字段，因为OrderItem实体中没有这个字段
						// 这里我们通过扩展OrderItem来添加这个字段
						item.setHasReview(hasReview);
						System.out.println("订单项 " + item.getId() + " 的评价状态: " + (hasReview ? "已评价" : "未评价"));
					} catch (Exception e) {
						System.err.println("检查订单项 " + item.getId() + " 的评价状态时出错: " + e.getMessage());
						item.setHasReview(false);
					}
				}
			}
			
			return orders;
		} catch (Exception e) {
			System.err.println("查询用户订单时发生错误: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	/** 获取订单详情 */
	public Order getOrderDetail(String username, Long orderId) {
		User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
		Order order = orderRepository.findById(orderId).orElseThrow(() -> new BusinessException("订单不存在"));
		
		// 检查权限：用户只能查看自己的订单，商家只能查看自己店铺的订单
		if (user.getRole() == User.UserRole.MERCHANT) {
			// 商家用户：既可以以商家身份查看店铺订单，也可以以买家身份查看自己下的订单
			Merchant merchant = merchantRepository.findByUserId(user.getId())
				.orElseThrow(() -> new BusinessException("商家信息不存在"));
			boolean isMerchantOrder = order.getMerchant() != null && order.getMerchant().getId().equals(merchant.getId());
			boolean isBuyerOwn = order.getUser() != null && order.getUser().getId().equals(user.getId());
			if (!isMerchantOrder && !isBuyerOwn) {
				throw new BusinessException("无权查看此订单");
			}
		} else if (user.getRole() == User.UserRole.USER) {
			// 普通用户：检查订单是否属于自己
			if (!order.getUser().getId().equals(user.getId())) {
				throw new BusinessException("无权查看此订单");
			}
		} else {
			throw new BusinessException("用户角色不支持");
		}
		
		// 加载订单项信息
		List<OrderItem> orderItems = orderItemRepository.findByOrder_IdWithProduct(order.getId());
		System.out.println("订单 " + orderId + " 的订单项数量: " + orderItems.size());
		for (OrderItem item : orderItems) {
			System.out.println("订单项: " + item.getProductNameSnapshot() + ", 数量: " + item.getQuantity());
		}
		order.setOrderItems(orderItems);
		
		// 为每个订单项设置评价状态（使用更安全的方式）
		Map<Long, Boolean> reviewStatusMap = new HashMap<>();
		for (OrderItem item : orderItems) {
			try {
				// 使用COUNT查询来避免重复数据问题
				Long reviewCount = reviewRepository.countByOrderItem_Id(item.getId());
				boolean hasReview = reviewCount > 0;
				reviewStatusMap.put(item.getId(), hasReview);
				System.out.println("订单项 " + item.getId() + " 的评价状态: " + (hasReview ? "已评价" : "未评价"));
			} catch (Exception e) {
				System.err.println("检查订单项 " + item.getId() + " 的评价状态时出错: " + e.getMessage());
				reviewStatusMap.put(item.getId(), false);
			}
		}
		
		// 加载发货信息
		Optional<Shipment> shipment = shipmentRepository.findByOrderId(order.getId());
		if (shipment.isPresent()) {
			order.setShipment(shipment.get());
		}
		
		return order;
	}

	/** 取消订单 */
	@Transactional
	public void cancelOrder(String username, Long orderId) {
		User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
		Order order = orderRepository.findById(orderId).orElseThrow(() -> new BusinessException("订单不存在"));
		
		if (!order.getUser().getId().equals(user.getId())) {
			throw new BusinessException("无权操作此订单");
		}
		
		if (order.getStatus() != Order.OrderStatus.UNPAID) {
			throw new BusinessException("只能取消未支付的订单");
		}
		
		order.setStatus(Order.OrderStatus.CLOSED);
		orderRepository.save(order);
	}

	/** 确认收货 */
	@Transactional
	public void confirmReceive(String username, Long orderId) {
		User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
		Order order = orderRepository.findById(orderId).orElseThrow(() -> new BusinessException("订单不存在"));
		
		if (!order.getUser().getId().equals(user.getId())) {
			throw new BusinessException("无权操作此订单");
		}
		
		if (order.getStatus() != Order.OrderStatus.SHIPPED) {
			throw new BusinessException("订单状态不正确");
		}
		
		order.setStatus(Order.OrderStatus.FINISHED);
		orderRepository.save(order);
	}

	/** 获取订单统计信息 */
	public OrderSummary getOrderSummary(String username) {
		User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
		List<Order> orders = orderRepository.findAllByUserId(user.getId());
		
		int totalOrders = orders.size();
		int unpaidOrders = 0;
		int paidOrders = 0;
		int shippedOrders = 0;
		int finishedOrders = 0;
		int cancelledOrders = 0;
		
		for (Order order : orders) {
			switch (order.getStatus()) {
				case UNPAID:
					unpaidOrders++;
					break;
				case PAID:
					paidOrders++;
					break;
				case SHIPPED:
					shippedOrders++;
					break;
				case FINISHED:
					finishedOrders++;
					break;
				case CLOSED:
					cancelledOrders++;
					break;
			}
		}
		
		return new OrderSummary(totalOrders, unpaidOrders, paidOrders, shippedOrders, finishedOrders, cancelledOrders);
	}

	/** 根据订单号获取订单 */
	public Order getOrderByOrderNo(String orderNo) {
		return orderRepository.findByOrderNo(orderNo).orElse(null);
	}

	/**
	 * 网关回调：将订单标记为已支付（幂等）
	 */
	@Transactional
	public Order markPaidByGateway(String orderNo, String tradeNo) {
		Order order = orderRepository.findByOrderNo(orderNo)
			.orElseThrow(() -> new BusinessException("订单不存在"));
		if (order.getStatus() == Order.OrderStatus.PAID
			|| order.getStatus() == Order.OrderStatus.SHIPPED
			|| order.getStatus() == Order.OrderStatus.FINISHED) {
			return order; // 幂等返回
		}
		if (order.getStatus() != Order.OrderStatus.UNPAID) {
			throw new BusinessException("订单状态不支持标记为已支付");
		}
		
		// 扣减库存（支付成功后）
		List<OrderItem> orderItems = orderItemRepository.findByOrder_Id(order.getId());
		for (OrderItem oi : orderItems) {
			Product p = oi.getProduct();
			if (p.getStock() == null || p.getStock() < oi.getQuantity()) {
				throw new BusinessException(400, "库存不足: " + p.getName());
			}
			p.setStock(p.getStock() - oi.getQuantity());
			productRepository.save(p);
		}
		
		order.setStatus(Order.OrderStatus.PAID);
		order.setPayType("ALIPAY");
		order.setPayTime(LocalDateTime.now());
		// 可以在订单上扩展支付交易号字段；如无则忽略
		return orderRepository.save(order);
	}
}


