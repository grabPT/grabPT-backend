package com.grabpt.repository;

import com.grabpt.domain.entity.Category;
import com.grabpt.domain.entity.Requestions;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category,Long> {

	@Query("""
			SELECT r
			FROM Requestions r
			JOIN FETCH r.user u
			WHERE r.category.code =: categoryCode
			ORDER BY r.createdAt DESC
		""")
	List<Requestions> findTop6RequestionsByCategory(String categoryCode, Pageable pageable);
}
