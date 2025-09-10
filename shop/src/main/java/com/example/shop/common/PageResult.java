package com.example.shop.common;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private Long total;
    private Integer totalPages;
    private Integer currentPage;
    private Integer pageSize;
    private List<T> list;

    public PageResult(Long total, Integer totalPages, Integer currentPage, Integer pageSize, List<T> list) {
        this.total = total;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.list = list;
    }
}
