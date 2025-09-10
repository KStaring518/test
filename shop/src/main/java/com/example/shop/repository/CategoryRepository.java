package com.example.shop.repository;

import com.example.shop.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

	List<Category> findByParentIsNullOrderBySortOrder();

	List<Category> findByParent_IdOrderBySortOrder(Long parentId);

	@Query("SELECT c FROM Category c WHERE c.status = 'ENABLED' ORDER BY c.sortOrder")
	List<Category> findEnabledCategories();

	@Query("SELECT c.id FROM Category c WHERE c.id = :rootId OR c.parent.id = :rootId")
	List<Long> findSelfAndDirectChildrenIds(Long rootId);
}


