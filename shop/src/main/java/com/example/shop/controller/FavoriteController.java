package com.example.shop.controller;

import com.example.shop.common.PageResult;
import com.example.shop.common.Result;
import com.example.shop.entity.Favorite;
import com.example.shop.vo.FavoriteItemVO;
import com.example.shop.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @PostMapping("/add/{productId}")
    public Result<Favorite> add(Authentication auth, @PathVariable Long productId) {
        return Result.success(favoriteService.addFavorite(auth.getName(), productId));
    }

    @DeleteMapping("/remove/{productId}")
    public Result<Void> remove(Authentication auth, @PathVariable Long productId) {
        favoriteService.removeFavorite(auth.getName(), productId);
        return Result.success();
    }

    @GetMapping("/check/{productId}")
    public Result<Boolean> check(Authentication auth, @PathVariable Long productId) {
        return Result.success(favoriteService.isFavorite(auth.getName(), productId));
    }

    @GetMapping("/list")
    public Result<PageResult<FavoriteItemVO>> list(Authentication auth,
                                             @RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer size) {
        Page<FavoriteItemVO> p = favoriteService.listFavoriteItems(auth.getName(), page, size);
        return Result.success(new PageResult<>(p.getTotalElements(), p.getTotalPages(), page, size, p.getContent()));
    }
}


