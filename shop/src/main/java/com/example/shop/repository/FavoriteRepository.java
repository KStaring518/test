package com.example.shop.repository;

import com.example.shop.entity.Favorite;
import com.example.shop.entity.Product;
import com.example.shop.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByUserAndProduct(User user, Product product);
    Page<Favorite> findByUser(User user, Pageable pageable);
    long countByUser(User user);
    void deleteByUserAndProduct(User user, Product product);
}


