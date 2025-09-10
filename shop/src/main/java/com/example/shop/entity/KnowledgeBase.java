package com.example.shop.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_base")
@Data
public class KnowledgeBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "category", nullable = false, length = 50)
    private String category;
    
    @Column(name = "question", columnDefinition = "TEXT", nullable = false)
    private String question;
    
    @Column(name = "answer", columnDefinition = "TEXT", nullable = false)
    private String answer;
    
    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords;
    
    @Column(name = "priority")
    private Integer priority = 0;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
