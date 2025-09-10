package com.example.shop.repository;

import com.example.shop.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

	Optional<Order> findByOrderNo(String orderNo);

	@Query(value = "SELECT o FROM Order o JOIN o.user WHERE o.user.id = :userId ORDER BY o.createdAt DESC",
	       countQuery = "SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
	Page<Order> findByUserId(@Param("userId") Long userId, Pageable pageable);

	@Query("SELECT o FROM Order o JOIN FETCH o.user WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
	List<Order> findAllByUserId(@Param("userId") Long userId);

	@Query(value = "SELECT o FROM Order o JOIN o.user WHERE o.user.id = :userId AND o.status = :status ORDER BY o.createdAt DESC",
	       countQuery = "SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.status = :status")
	Page<Order> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Order.OrderStatus status, Pageable pageable);

	@Query(value = "SELECT o FROM Order o WHERE o.status = :status",
	       countQuery = "SELECT COUNT(o) FROM Order o WHERE o.status = :status")
	Page<Order> findByStatus(@Param("status") Order.OrderStatus status, Pageable pageable);

	@Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :startTime AND :endTime")
	Long countByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

	@Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
	Long countByStatus(@Param("status") Order.OrderStatus status);

	@Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
	Long countByUserId(@Param("userId") Long userId);

	@Query("SELECT COUNT(o) FROM Order o WHERE o.merchant.id = :merchantId AND o.createdAt BETWEEN :startTime AND :endTime")
	Long countByMerchantIdAndCreatedAtBetween(@Param("merchantId") Long merchantId,
	                                         @Param("startTime") LocalDateTime start,
	                                         @Param("endTime") LocalDateTime end);

	@Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = :status AND o.createdAt BETWEEN :startTime AND :endTime")
	BigDecimal sumTotalAmountByStatusAndCreatedAtBetween(@Param("status") Order.OrderStatus status,
	                                                 @Param("startTime") LocalDateTime startTime,
	                                                 @Param("endTime") LocalDateTime endTime);

	// 商家相关查询方法
	@Query(value = "SELECT o FROM Order o JOIN o.user WHERE o.merchant.id = :merchantId ORDER BY o.createdAt DESC",
	       countQuery = "SELECT COUNT(o) FROM Order o WHERE o.merchant.id = :merchantId")
	Page<Order> findByMerchantId(@Param("merchantId") Long merchantId, Pageable pageable);
	
	@Query(value = "SELECT o FROM Order o JOIN o.user WHERE o.merchant.id = :merchantId AND o.status = :status ORDER BY o.createdAt DESC",
	       countQuery = "SELECT COUNT(o) FROM Order o WHERE o.merchant.id = :merchantId AND o.status = :status")
	Page<Order> findByMerchantIdAndStatus(@Param("merchantId") Long merchantId, @Param("status") Order.OrderStatus status, Pageable pageable);
	
	Optional<Order> findByIdAndMerchantId(Long id, Long merchantId);
	
	@Query("SELECT COUNT(o) FROM Order o WHERE o.merchant.id = :merchantId")
	int countByMerchantId(@Param("merchantId") Long merchantId);
	
	@Query("SELECT COUNT(o) FROM Order o WHERE o.merchant.id = :merchantId AND o.status = :status")
	int countByMerchantIdAndStatus(@Param("merchantId") Long merchantId, @Param("status") Order.OrderStatus status);
	
	@Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.merchant.id = :merchantId AND o.status = :status")
	BigDecimal sumTotalAmountByMerchantIdAndStatus(@Param("merchantId") Long merchantId, @Param("status") Order.OrderStatus status);
	
	@Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.merchant.id = :merchantId AND o.status = :status AND o.createdAt BETWEEN :startTime AND :endTime")
	BigDecimal sumTotalAmountByMerchantIdAndStatusAndCreatedAtBetween(
            @Param("merchantId") Long merchantId, 
            @Param("status") Order.OrderStatus status, 
            @Param("startTime") LocalDateTime startTime, 
            @Param("endTime") LocalDateTime endTime
    );
}


