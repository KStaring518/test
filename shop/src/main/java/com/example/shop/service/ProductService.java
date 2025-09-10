package com.example.shop.service;

import com.example.shop.common.BusinessException;
import com.example.shop.dto.ProductCreateDTO;
import com.example.shop.entity.Category;
import com.example.shop.entity.Product;
import com.example.shop.repository.CategoryRepository;
import com.example.shop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Service
public class ProductService {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	public Page<Product> listOnSale(Integer page, Integer size, String keyword, Long categoryId, String sort,
	                               BigDecimal minPrice, BigDecimal maxPrice) {
		PageRequest pageRequest = PageRequest.of(page - 1, size, createSort(sort));
		
		Page<Product> products;
		boolean hasPrice = (minPrice != null && maxPrice != null);
		if (categoryId != null && categoryId > 0) {
			// 查询该分类“自身 + 所有子孙分类”的商品
			List<Long> ids = getSelfAndDescendantCategoryIds(categoryId);
			products = productRepository.findByCategoryIdsAndStatus(ids, Product.ProductStatus.ON_SALE, pageRequest);
		} else if (keyword != null && !keyword.trim().isEmpty()) {
			products = productRepository.searchByKeyword(keyword.trim(), pageRequest);
		} else if (hasPrice) {
			products = productRepository.findByPriceRange(minPrice, maxPrice, pageRequest);
		} else {
			products = productRepository.findByStatus(Product.ProductStatus.ON_SALE, pageRequest);
		}
		
		// 二次在内存中做价格过滤（当与分类/关键词组合时）
		List<Product> filtered = products.getContent().stream()
			.filter(product -> product.getMerchant() != null)
			.filter(p -> !hasPrice || (p.getPrice().compareTo(minPrice) >= 0 && p.getPrice().compareTo(maxPrice) <= 0))
			.collect(Collectors.toList());
		
		return new PageImpl<>(filtered, pageRequest, products.getTotalElements());
	}

	/**
	 * 获取分类自身及其所有子孙分类ID
	 */
	private List<Long> getSelfAndDescendantCategoryIds(Long rootId) {
		List<Long> result = new ArrayList<>();
		collectCategoryIdsRecursively(rootId, result);
		return result;
	}

	private void collectCategoryIdsRecursively(Long categoryId, List<Long> acc) {
		acc.add(categoryId);
		List<Category> children = categoryRepository.findByParent_IdOrderBySortOrder(categoryId);
		for (Category child : children) {
			collectCategoryIdsRecursively(child.getId(), acc);
		}
	}

	private Sort createSort(String sort) {
		if (sort == null || sort.isEmpty()) {
			return Sort.by(Sort.Direction.DESC, "createdAt"); // 默认按创建时间倒序
		}
		
		switch (sort) {
			case "price_asc":
				return Sort.by(Sort.Direction.ASC, "price");
			case "price_desc":
				return Sort.by(Sort.Direction.DESC, "price");
			case "created_desc":
				return Sort.by(Sort.Direction.DESC, "createdAt");
			default:
				return Sort.by(Sort.Direction.DESC, "createdAt");
		}
	}

	public Product create(ProductCreateDTO dto) {
		Category category = categoryRepository.findById(dto.getCategoryId())
				.orElseThrow(() -> new BusinessException(400, "分类不存在"));
		Product p = new Product();
		p.setName(dto.getName());
		p.setSubtitle(dto.getSubtitle());
		p.setCategory(category);
		p.setCoverImage(dto.getCoverImage());
		p.setPrice(dto.getPrice());
		p.setUnit(dto.getUnit());
		p.setStock(dto.getStock());
		p.setStatus(Product.ProductStatus.ON_SALE);
		p.setDescription(dto.getDescription());
		return productRepository.save(p);
	}

	public Product getProduct(Long id) {
		return productRepository.findById(id)
				.orElseThrow(() -> new BusinessException(400, "商品不存在"));
	}
}


