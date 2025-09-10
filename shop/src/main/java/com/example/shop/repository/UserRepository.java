package com.example.shop.repository;

import com.example.shop.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByUsername(String username);

	boolean existsByUsername(String username);

	@Query("SELECT u FROM User u WHERE u.role = :role")
	List<User> findByRole(@Param("role") User.UserRole role);

	@Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
	Long countByRole(@Param("role") User.UserRole role);

	@Query("SELECT u FROM User u WHERE (:role is null or u.role = :role) AND (:status is null or u.status = :status) AND (:kw is null or u.username LIKE CONCAT('%',:kw,'%') OR :kw is null or u.nickname LIKE CONCAT('%',:kw,'%'))")
	Page<User> searchUsers(@Param("kw") String keyword,
						  @Param("role") User.UserRole role,
						  @Param("status") User.UserStatus status,
						  Pageable pageable);
}


