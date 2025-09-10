package com.example.shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.List;

@Repository
public interface AdminUserRepository extends JpaRepository<com.example.shop.entity.User, Long> {

	interface UserAdminProjection {
		Long getId();
		String getUsername();
		String getNickname();
		String getPhone();
		String getEmail();
		String getAvatarUrl();
		String getRole();
		String getStatus();
		Timestamp getCreatedAt();
	}

	@Query(value = "select id, username, nickname, phone, email, avatar_url as avatarUrl, role, status, created_at as createdAt from users order by id desc", nativeQuery = true)
	List<UserAdminProjection> listAllUsers();

	@Transactional
	@Modifying
	@Query(value = "update users set status = :status where id = :id", nativeQuery = true)
	int updateUserStatus(@Param("id") Long id, @Param("status") String status);

	@Transactional
	@Modifying
	@Query(value = "update users set password_hash = :password where id = :id", nativeQuery = true)
	int resetPassword(@Param("id") Long id, @Param("password") String passwordHash);
}


