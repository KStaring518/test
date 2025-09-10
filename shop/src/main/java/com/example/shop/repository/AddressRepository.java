package com.example.shop.repository;

import com.example.shop.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

	List<Address> findByUser_Id(Long userId);

	Optional<Address> findByIdAndUser_Id(Long id, Long userId);

	/**
	 * 查找用户的默认地址
	 */
	Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);

	/**
	 * 清除用户的所有默认地址
	 */
	@Modifying
	@Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
	void clearDefaultAddress(@Param("userId") Long userId);
}


