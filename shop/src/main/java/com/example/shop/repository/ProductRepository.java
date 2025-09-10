package com.example.shop.repository;

import com.example.shop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

	// 修复：使用category.id而不是categoryId
	@Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.status = :status")
	Page<Product> findByCategoryIdAndStatus(@Param("categoryId") Long categoryId, @Param("status") Product.ProductStatus status, Pageable pageable);

	@Query("SELECT p FROM Product p WHERE p.category.id IN :categoryIds AND p.status = :status")
	Page<Product> findByCategoryIdsAndStatus(@Param("categoryIds") List<Long> categoryIds, @Param("status") Product.ProductStatus status, Pageable pageable);

	@Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% OR p.description LIKE %:keyword%")
	Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

	@Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
	Page<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);

	@Query("SELECT p FROM Product p WHERE p.stock < :minStock")
	List<Product> findLowStockProducts(@Param("minStock") Integer minStock);

	@Query("SELECT p FROM Product p WHERE p.status = :status")
	Page<Product> findByStatus(@Param("status") Product.ProductStatus status, Pageable pageable);

	// 商家相关查询方法
	Page<Product> findByMerchantId(Long merchantId, Pageable pageable);
	
	Page<Product> findByMerchantIdAndStatus(Long merchantId, Product.ProductStatus status, Pageable pageable);
	
	Page<Product> findByMerchantIdAndCategoryId(Long merchantId, Long categoryId, Pageable pageable);
	
	Page<Product> findByMerchantIdAndNameContaining(Long merchantId, String name, Pageable pageable);
	
	Page<Product> findByMerchantIdAndNameContainingAndStatus(Long merchantId, String name, Product.ProductStatus status, Pageable pageable);
	
	Page<Product> findByMerchantIdAndNameContainingAndCategoryId(Long merchantId, String name, Long categoryId, Pageable pageable);
	
	Page<Product> findByMerchantIdAndCategoryIdAndStatus(Long merchantId, Long categoryId, Product.ProductStatus status, Pageable pageable);
	
	Page<Product> findByMerchantIdAndNameContainingAndCategoryIdAndStatus(Long merchantId, String name, Long categoryId, Product.ProductStatus status, Pageable pageable);
	
	Optional<Product> findByIdAndMerchantId(Long id, Long merchantId);
	
	int countByMerchantId(Long merchantId);
	
	int countByMerchantIdAndStatus(Long merchantId, Product.ProductStatus status);
}


