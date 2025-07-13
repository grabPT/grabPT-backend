package com.grabpt.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.grabpt.domain.entity.Users;

public interface UserRepository extends JpaRepository<Users, Long> {
	Page<Users> findAllByProProfile_Categories_Code(String categoryCode, Pageable pageable);
}
