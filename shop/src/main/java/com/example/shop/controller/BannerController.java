package com.example.shop.controller;

import com.example.shop.common.Result;
import com.example.shop.entity.Banner;
import com.example.shop.repository.BannerRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/banners")
public class BannerController {

    @Autowired
    private BannerRepository bannerRepository;

    @GetMapping("/public/list")
    public Result<List<Banner>> publicList() {
        return Result.success(bannerRepository.findByStatusOrderBySortOrder(Banner.BannerStatus.ENABLED));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/list")
    public Result<List<Banner>> adminList() {
        return Result.success(bannerRepository.findAllByOrderBySortOrderAsc());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/create")
    public Result<Banner> create(@RequestBody Banner banner) {
        if (banner.getStatus() == null) banner.setStatus(Banner.BannerStatus.ENABLED);
        if (banner.getSortOrder() == null) banner.setSortOrder(0);
        return Result.success(bannerRepository.save(banner));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/{id}")
    public Result<Banner> update(@PathVariable Long id, @RequestBody Banner banner) {
        banner.setId(id);
        return Result.success(bannerRepository.save(banner));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/order")
    public Result<Void> updateOrder(@RequestBody List<Map<String, Object>> orderData) {
        for (Map<String, Object> item : orderData) {
            Long id = Long.valueOf(item.get("id").toString());
            Integer sortOrder = Integer.valueOf(item.get("sortOrder").toString());
            Banner banner = bannerRepository.findById(id).orElse(null);
            if (banner != null) {
                banner.setSortOrder(sortOrder);
                bannerRepository.save(banner);
            }
        }
        return Result.success();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bannerRepository.deleteById(id);
        return Result.success();
    }
}


