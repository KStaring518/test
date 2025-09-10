package com.example.shop.repository;

import com.example.shop.entity.KnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {
    
    List<KnowledgeBase> findByCategoryOrderByPriorityDesc(String category);
    
    @Query(value = "SELECT * FROM knowledge_base WHERE MATCH(question, answer) AGAINST(:keyword IN NATURAL LANGUAGE MODE) ORDER BY priority DESC", nativeQuery = true)
    List<KnowledgeBase> searchByKeyword(@Param("keyword") String keyword);
    
    @Query(value = "SELECT * FROM knowledge_base WHERE question LIKE %:keyword% OR answer LIKE %:keyword% OR keywords LIKE %:keyword% ORDER BY priority DESC", nativeQuery = true)
    List<KnowledgeBase> searchByText(@Param("keyword") String keyword);
    
    List<KnowledgeBase> findByPriorityGreaterThanOrderByPriorityDesc(Integer priority);
}
