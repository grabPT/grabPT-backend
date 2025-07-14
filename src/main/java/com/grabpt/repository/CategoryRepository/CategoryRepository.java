package com.grabpt.repository.CategoryRepository;

import com.grabpt.domain.entity.Category;
import com.grabpt.domain.entity.Requestions;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category,Long> {
}
