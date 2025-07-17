package com.grabpt.repository.RequestionRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.grabpt.domain.entity.Requestions;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RequestionRepository extends JpaRepository<Requestions, Long> {
	Page<Requestions> findAllByUserId(Long userId, Pageable pageable);

	@Query("""
			SELECT r
			FROM Requestions r
			JOIN FETCH r.user u
			WHERE r.category.code =:categoryCode
			ORDER BY r.createdAt DESC
		""")
	List<Requestions> findTop6RequestionsByCategory(@Param("categoryCode") String categoryCode, Pageable pageable);


}
