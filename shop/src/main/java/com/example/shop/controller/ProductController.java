package com.example.shop.controller;

import com.example.shop.common.PageResult;
import com.example.shop.common.Result;
import com.example.shop.dto.ProductCreateDTO;
import com.example.shop.entity.Product;
import com.example.shop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import com.example.shop.entity.Category;
import com.example.shop.repository.CategoryRepository;
import java.math.BigDecimal;

@RestController
@RequestMapping("/products")
public class ProductController {

	@Autowired
	private ProductService productService;

	@Autowired
	private CategoryRepository categoryRepository;

	@GetMapping("/public/list")
	public Result<PageResult<Product>> list(@RequestParam(defaultValue = "1") Integer page,
	                                      @RequestParam(defaultValue = "10") Integer size,
	                                      @RequestParam(required = false) String keyword,
	                                      @RequestParam(required = false) Long categoryId,
	                                      @RequestParam(required = false) String sort,
	                                      @RequestParam(required = false) BigDecimal minPrice,
	                                      @RequestParam(required = false) BigDecimal maxPrice) {
		Page<Product> p = productService.listOnSale(page, size, keyword, categoryId, sort, minPrice, maxPrice);
		return Result.success(new PageResult<>(p.getTotalElements(), p.getTotalPages(), page, size, p.getContent()));
	}

	@GetMapping("/public/{id}")
	public Result<Product> getProduct(@PathVariable Long id) {
		return Result.success(productService.getProduct(id));
	}

	@PostMapping("/create")
	public Result<Product> create(@Validated @RequestBody ProductCreateDTO dto) {
		return Result.success(productService.create(dto));
	}

	/**
	 * 公开的商品搜索API（供智能客服使用）
	 */
	@GetMapping("/public/search")
	public Result<List<Product>> publicSearch(@RequestParam(required = false) String keyword,
	                                         @RequestParam(required = false) Long categoryId,
	                                         @RequestParam(defaultValue = "10") Integer limit) {
		List<Product> products;
		if (categoryId != null && categoryId > 0) {
			// 按分类查询
			products = productService.listOnSale(1, limit, null, categoryId, null, null, null).getContent();
		} else if (keyword != null && !keyword.trim().isEmpty()) {
			// 按关键词查询
			products = productService.listOnSale(1, limit, keyword.trim(), null, null, null, null).getContent();
		} else {
			// 获取热门商品
			products = productService.listOnSale(1, limit, null, null, null, null, null).getContent();
		}
		
		// 过滤掉没有有效商家的商品
		products = products.stream()
			.filter(product -> product.getMerchant() != null)
			.collect(Collectors.toList());
		
		return Result.success(products);
	}

	/**
	 * 获取商品分类列表（供智能客服使用）
	 */
	@GetMapping("/public/categories")
	public Result<List<Category>> publicCategories() {
		return Result.success(categoryRepository.findEnabledCategories());
	}
}


