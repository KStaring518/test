package com.example.shop.repository;

import com.example.shop.entity.Merchant;
import com.example.shop.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {

	Optional<Merchant> findByUserId(Long userId);

	@Query("SELECT m FROM Merchant m WHERE m.status = :status")
	List<Merchant> findByStatus(@Param("status") Merchant.MerchantStatus status);

	@Query("SELECT COUNT(m) FROM Merchant m WHERE m.status = :status")
	Long countByStatus(@Param("status") Merchant.MerchantStatus status);

	Optional<Merchant> findByUser(User user);

	boolean existsByUser(User user);

	Page<Merchant> findByStatus(Merchant.MerchantStatus status, Pageable pageable);

	Page<Merchant> findByShopNameContaining(String shopName, Pageable pageable);
}


