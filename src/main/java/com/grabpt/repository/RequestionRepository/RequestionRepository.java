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

	/**
	 * 정확히 전달받은 fullRegion(예: "서울 강남구 역삼동")으로 시작하는 location만 집계.
	 * - 점진적 확장 없음(동→구→시로 내려가지 않음)
	 * - sessionCount > 0만 집계
	 * - category.name으로 필터
	 * - 실수 평균 보장
	 */
	@Query("""
		    select coalesce(avg(1.0 * r.price / r.sessionCount), 0.0)
		    from Requestions r
		    where r.sessionCount > 0
		      and r.category.name = :categoryName
		      and r.location like concat(:fullRegion, '%')
		""")
	double avgUnitPriceByCategoryAndRegion(
		@Param("categoryName") String categoryName,
		@Param("fullRegion") String fullRegion
	);

	@Query("""
		    select count(r)
		    from Requestions r
		    where r.sessionCount > 0
		      and r.category.name = :categoryName
		      and r.location like concat(:fullRegion, '%')
		""")
	long countByCategoryAndRegion(
		@Param("categoryName") String categoryName,
		@Param("fullRegion") String fullRegion
	);
}
