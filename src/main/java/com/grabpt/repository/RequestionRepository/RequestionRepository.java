package com.grabpt.repository.RequestionRepository;

import static jakarta.persistence.LockModeType.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grabpt.domain.entity.Category;
import com.grabpt.domain.entity.Requestions;

public interface RequestionRepository extends JpaRepository<Requestions, Long> {
	Page<Requestions> findAllByUserId(Long userId, Pageable pageable);

	Page<Requestions> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

	@Query("""
			SELECT r
			FROM Requestions r
			JOIN FETCH r.user u
			WHERE r.category.code =:categoryCode
			ORDER BY r.createdAt DESC
		""")
	List<Requestions> findTop8RequestionsByCategory(@Param("categoryCode") String categoryCode, Pageable pageable);

	// 최신순
	Page<Requestions> findByLocationOrderByCreatedAtDesc(String location, Pageable pageable);

	// 최신순(주소변경)
	Page<Requestions> findByLocationStartingWithAndCategoryOrderByCreatedAtDesc(String locationPrefix,
		Category category, Pageable pageable);

	// 가격 높은 순(주소변경)
	Page<Requestions> findByLocationStartingWithAndCategoryOrderByPriceDesc(String locationPrefix, Category category,
		Pageable pageable);

	// 기본 요청서 조회
	Page<Requestions> findByLocation(String locationPrefix, Pageable pageable);

	// 본인 요청서 조회
	Page<Requestions> findAllByUserEmail(String email, Pageable pageable);

	@Lock(PESSIMISTIC_WRITE)
	@Query("select r from Requestions r where r.id = :id")
	Optional<Requestions> findByIdForUpdate(@Param("id") Long id);
}
