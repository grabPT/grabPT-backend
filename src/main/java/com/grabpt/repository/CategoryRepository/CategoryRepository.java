package com.grabpt.repository.CategoryRepository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grabpt.domain.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
