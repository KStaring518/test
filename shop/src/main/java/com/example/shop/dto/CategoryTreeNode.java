package com.example.shop.dto;

import com.example.shop.entity.Category;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryTreeNode {
    private Long id;
    private String name;
    private Long parentId;
    private Integer sortOrder;
    private Category.CategoryStatus status;
    private List<CategoryTreeNode> children = new ArrayList<>();

    public static CategoryTreeNode from(Category c) {
        CategoryTreeNode n = new CategoryTreeNode();
        n.setId(c.getId());
        n.setName(c.getName());
        n.setParentId(c.getParent() == null ? null : c.getParent().getId());
        n.setSortOrder(c.getSortOrder());
        n.setStatus(c.getStatus());
        return n;
    }
}


