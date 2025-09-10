package com.example.shop.controller;

import com.example.shop.common.BusinessException;
import com.example.shop.common.Result;
import com.example.shop.entity.OrderItem;
import com.example.shop.entity.Review;
import com.example.shop.entity.User;
import com.example.shop.repository.OrderItemRepository;
import com.example.shop.repository.ReviewRepository;
import com.example.shop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/public/product/{productId}")
    public Result<List<Review>> listByProduct(@PathVariable Long productId) {
        return Result.success(reviewRepository.findByOrderItem_Product_Id(productId));
    }

    @PreAuthorize("hasAnyRole('USER','MERCHANT')")
    @PostMapping("/create")
    public Result<Review> create(Authentication authentication,
                                 @RequestParam Long orderItemId,
                                 @RequestParam Integer rating,
                                 @RequestParam(required = false) String content,
                                 @RequestParam(required = false) String images,
                                 @RequestParam(name = "anonymous", required = false, defaultValue = "false") Boolean anonymous) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new BusinessException("评分必须在1-5之间");
        }

        // 防止重复评价：一个订单项只允许一条评价
        if (reviewRepository.existsByOrderItem_Id(orderItemId)) {
            throw new BusinessException("该商品已评价过");
        }
        OrderItem oi = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new BusinessException("订单明细不存在"));

        Object principal = authentication.getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        Review r = new Review();
        r.setOrderItem(oi);
        r.setUser(user);
        r.setRating(rating);
        r.setContent(content);
        r.setImages(images);
        r.setIsAnonymous(Boolean.TRUE.equals(anonymous));
        return Result.success(reviewRepository.save(r));
    }
}


