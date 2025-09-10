package com.example.shop.controller;

import com.example.shop.common.Result;
import com.example.shop.entity.Category;
import com.example.shop.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

	@Autowired
	private CategoryRepository categoryRepository;

	@GetMapping("/public/tree")
	public Result<List<Category>> tree() {
		// 返回所有启用的分类，包括顶级分类和子分类
		return Result.success(categoryRepository.findEnabledCategories());
	}
}


