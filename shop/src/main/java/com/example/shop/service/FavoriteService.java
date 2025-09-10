package com.example.shop.service;

import com.example.shop.common.BusinessException;
import com.example.shop.entity.Favorite;
import com.example.shop.entity.Product;
import com.example.shop.entity.User;
import com.example.shop.repository.FavoriteRepository;
import com.example.shop.repository.ProductRepository;
import com.example.shop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.shop.vo.FavoriteItemVO;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public Favorite addFavorite(String username, Long productId) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
        Product product = productRepository.findById(productId).orElseThrow(() -> new BusinessException("商品不存在"));
        return favoriteRepository.findByUserAndProduct(user, product)
                .orElseGet(() -> {
                    Favorite f = new Favorite();
                    f.setUser(user);
                    f.setProduct(product);
                    return favoriteRepository.save(f);
                });
    }

    @Transactional
    public void removeFavorite(String username, Long productId) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
        Product product = productRepository.findById(productId).orElseThrow(() -> new BusinessException("商品不存在"));
        favoriteRepository.deleteByUserAndProduct(user, product);
    }

    public boolean isFavorite(String username, Long productId) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
        Product product = productRepository.findById(productId).orElseThrow(() -> new BusinessException("商品不存在"));
        return favoriteRepository.findByUserAndProduct(user, product).isPresent();
    }

    public Page<Favorite> listFavorites(String username, Integer page, Integer size) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
        return favoriteRepository.findByUser(user, PageRequest.of(page - 1, size));
    }

    public Page<FavoriteItemVO> listFavoriteItems(String username, Integer page, Integer size) {
        Page<Favorite> p = listFavorites(username, page, size);
        return p.map(f -> {
            FavoriteItemVO vo = new FavoriteItemVO();
            vo.setFavoriteId(f.getId());
            vo.setProductId(f.getProduct().getId());
            vo.setName(f.getProduct().getName());
            vo.setSubtitle(f.getProduct().getSubtitle());
            vo.setCoverImage(f.getProduct().getCoverImage());
            vo.setPrice(f.getProduct().getPrice().doubleValue());
            vo.setUnit(f.getProduct().getUnit());
            vo.setStock(f.getProduct().getStock());
            vo.setCreatedAt(f.getCreatedAt());
            return vo;
        });
    }
}


