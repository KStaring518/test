package com.example.shop.repository;

import com.example.shop.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

	List<OrderItem> findByOrder_Id(Long orderId);

	@Query("SELECT oi FROM OrderItem oi JOIN FETCH oi.product WHERE oi.order.id = :orderId")
	List<OrderItem> findByOrder_IdWithProduct(@Param("orderId") Long orderId);

	@Query("SELECT oi.product.category.name, SUM(oi.subtotal) FROM OrderItem oi WHERE oi.order.status IN ('PAID','SHIPPED','FINISHED') GROUP BY oi.product.category.name ORDER BY SUM(oi.subtotal) DESC")
	List<Object[]> sumAmountByCategoryTop();

	@Query("SELECT oi.product.id, oi.product.name, SUM(oi.quantity) AS qty FROM OrderItem oi WHERE oi.order.merchant.id = :merchantId AND oi.order.status IN ('PAID','SHIPPED','FINISHED') GROUP BY oi.product.id, oi.product.name ORDER BY qty DESC")
	List<Object[]> topProductsByMerchant(@Param("merchantId") Long merchantId);
}


