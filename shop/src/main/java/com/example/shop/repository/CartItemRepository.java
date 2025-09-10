package com.example.shop.repository;

import com.example.shop.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

	List<CartItem> findByUserId(Long userId);

	Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);

	@Modifying
	@Query("DELETE FROM CartItem c WHERE c.user.id = :userId AND c.id IN :cartItemIds")
	void deleteByUserIdAndCartItemIds(@Param("userId") Long userId, @Param("cartItemIds") List<Long> cartItemIds);

	@Query("SELECT c FROM CartItem c WHERE c.user.id = :userId AND c.checked = true")
	List<CartItem> findCheckedItemsByUserId(@Param("userId") Long userId);
}


