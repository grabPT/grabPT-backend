package com.grabpt.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.grabpt.domain.entity.Requestions;

public interface requestionRepository extends JpaRepository<Requestions, Long> {
	Page<Requestions> findAllByUserId(Long userId, Pageable pageable);
}
