package com.example.shop.repository;

import com.example.shop.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByUser_IdOrderByCreatedAtDesc(Long userId);
    List<Review> findByOrderItem_Order_Id(Long orderId);
    
    @Query("SELECT r FROM Review r JOIN FETCH r.user WHERE r.orderItem.product.id = :productId ORDER BY r.createdAt DESC")
    List<Review> findByOrderItem_Product_Id(@Param("productId") Long productId);
    
    boolean existsByOrderItem_Id(Long orderItemId);
    Long countByOrderItem_Id(Long orderItemId);
}


