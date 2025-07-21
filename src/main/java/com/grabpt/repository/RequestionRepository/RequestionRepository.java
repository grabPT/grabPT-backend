package com.grabpt.repository.RequestionRepository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grabpt.domain.entity.Requestions;

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

	// 최신순
	Page<Requestions> findByLocationOrderByCreatedAtDesc(String location, Pageable pageable);

	// 가격 높은 순
	Page<Requestions> findByLocationOrderByPriceDesc(String location, Pageable pageable);
}
