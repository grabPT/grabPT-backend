package com.grabpt.repository.MatchingRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grabpt.domain.entity.Matching;
import com.grabpt.domain.enums.MatchingStatus;
import com.grabpt.domain.enums.PaymentStatus;

public interface MatchingRepository extends JpaRepository<Matching, Long> {
	/** 트레이너 활성 회원 수 */
	@Query("SELECT COUNT(DISTINCT m.requestion.user.id) FROM Matching m " +
		"JOIN m.suggestion s " +
		"WHERE s.proProfile.id = :proProfileId " +
		"AND m.status = :status")
	Long getActiveClients(@Param("proProfileId") Long proProfileId,
		@Param("status") MatchingStatus status);

	// 회원: 활성 계약(매칭) 수
	@Query("""
		    SELECT COUNT(DISTINCT m.id)
		    FROM Matching m
		    WHERE m.requestion.user.id = :userId
		      AND m.status = :status
		""")
	Long getActiveContractsByUser(@Param("userId") Long userId,
		@Param("status") MatchingStatus status);

	Matching findMatchingBySuggestionId(Long suggestionId);

	boolean existsByRequestionId(Long requestionId);

	boolean existsBySuggestionId(Long suggestionId);

	// 단건 조회 (필요 시)
	@Query("""
		    select m from Matching m
		    join fetch m.suggestion s
		    join fetch s.proProfile pp
		    where m.requestion.id = :requestionId
		""")
	Optional<Matching> findWithProByRequestionId(@Param("requestionId") Long requestionId);

	// 배치 조회 (N+1 회피)
	@Query("""
		    select m from Matching m
		    join m.suggestion s
		    join s.proProfile pp
		    where m.requestion.id in :requestionIds
		""")
	List<Matching> findAllWithProByRequestionIds(@Param("requestionIds") List<Long> requestionIds);

	// 계약 목록 조회 - 회원(USER) 기준
	@Query("""
		    SELECT m FROM Matching m
		    JOIN m.requestion req
		    WHERE req.user.id = :userId
		      AND m.status IN :statuses
		    ORDER BY m.matchedAt DESC
		""")
	Page<Matching> findContractsByUserId(@Param("userId") Long userId,
		@Param("statuses") List<MatchingStatus> statuses, Pageable pageable);

	// 계약 목록 조회 - 전문가(PRO) 기준
	@Query("""
		    SELECT m FROM Matching m
		    JOIN m.suggestion sug
		    JOIN sug.proProfile pp
		    WHERE pp.user.id = :userId
		      AND m.status IN :statuses
		    ORDER BY m.matchedAt DESC
		""")
	Page<Matching> findContractsByProUserId(@Param("userId") Long userId,
		@Param("statuses") List<MatchingStatus> statuses, Pageable pageable);

	// 카운트 - 회원(USER) 기준, 복수 상태
	Long countByRequestion_User_IdAndStatusIn(Long userId, List<MatchingStatus> statuses);

	// 카운트 - 전문가(PRO) 기준, 복수 상태
	@Query("""
		    SELECT COUNT(m) FROM Matching m
		    JOIN m.suggestion sug
		    WHERE sug.proProfile.user.id = :userId
		      AND m.status IN :statuses
		""")
	Long countContractsByProUserIdAndStatuses(@Param("userId") Long userId,
		@Param("statuses") List<MatchingStatus> statuses);

	// 목록 조회 - 회원(USER) 기준, PaymentStatus.OK인 Order가 존재하는 매칭
	@Query("""
		    SELECT m FROM Matching m
		    JOIN m.requestion req
		    WHERE req.user.id = :userId
		      AND m.status IN :statuses
		      AND EXISTS (SELECT o FROM m.orders o WHERE o.payment IS NOT NULL AND o.payment.status = com.grabpt.domain.enums.PaymentStatus.OK)
		    ORDER BY m.matchedAt DESC
		""")
	Page<Matching> findContractsByUserIdAndPaymentStatusOK(@Param("userId") Long userId,
		@Param("statuses") List<MatchingStatus> statuses, Pageable pageable);

	// 목록 조회 - 회원(USER) 기준, PaymentStatus.OK인 Order가 없는 매칭 (= READY)
	@Query("""
		    SELECT m FROM Matching m
		    JOIN m.requestion req
		    WHERE req.user.id = :userId
		      AND m.status IN :statuses
		      AND NOT EXISTS (SELECT o FROM m.orders o WHERE o.payment IS NOT NULL AND o.payment.status = com.grabpt.domain.enums.PaymentStatus.OK)
		    ORDER BY m.matchedAt DESC
		""")
	Page<Matching> findContractsByUserIdAndPaymentStatusReady(@Param("userId") Long userId,
		@Param("statuses") List<MatchingStatus> statuses, Pageable pageable);

	// 목록 조회 - 전문가(PRO) 기준, PaymentStatus.OK인 Order가 존재하는 매칭
	@Query("""
		    SELECT m FROM Matching m
		    JOIN m.suggestion sug
		    JOIN sug.proProfile pp
		    WHERE pp.user.id = :userId
		      AND m.status IN :statuses
		      AND EXISTS (SELECT o FROM m.orders o WHERE o.payment IS NOT NULL AND o.payment.status = com.grabpt.domain.enums.PaymentStatus.OK)
		    ORDER BY m.matchedAt DESC
		""")
	Page<Matching> findContractsByProUserIdAndPaymentStatusOK(@Param("userId") Long userId,
		@Param("statuses") List<MatchingStatus> statuses, Pageable pageable);

	// 목록 조회 - 전문가(PRO) 기준, PaymentStatus.OK인 Order가 없는 매칭 (= READY)
	@Query("""
		    SELECT m FROM Matching m
		    JOIN m.suggestion sug
		    JOIN sug.proProfile pp
		    WHERE pp.user.id = :userId
		      AND m.status IN :statuses
		      AND NOT EXISTS (SELECT o FROM m.orders o WHERE o.payment IS NOT NULL AND o.payment.status = com.grabpt.domain.enums.PaymentStatus.OK)
		    ORDER BY m.matchedAt DESC
		""")
	Page<Matching> findContractsByProUserIdAndPaymentStatusReady(@Param("userId") Long userId,
		@Param("statuses") List<MatchingStatus> statuses, Pageable pageable);

	// 카운트 - 회원(USER) 기준, PaymentStatus.OK인 Order가 존재하는 매칭 수
	@Query("""
		    SELECT COUNT(DISTINCT m.id) FROM Matching m
		    JOIN m.requestion req
		    WHERE req.user.id = :userId
		      AND m.status IN :statuses
		      AND EXISTS (SELECT o FROM m.orders o WHERE o.payment IS NOT NULL AND o.payment.status = :paymentStatus)
		""")
	Long countContractsByUserIdAndPaymentStatus(@Param("userId") Long userId,
		@Param("statuses") List<MatchingStatus> statuses,
		@Param("paymentStatus") PaymentStatus paymentStatus);

	// 카운트 - 회원(USER) 기준, PaymentStatus.OK인 Order가 없는 매칭 수 (= READY)
	@Query("""
		    SELECT COUNT(DISTINCT m.id) FROM Matching m
		    JOIN m.requestion req
		    WHERE req.user.id = :userId
		      AND m.status IN :statuses
		      AND NOT EXISTS (SELECT o FROM m.orders o WHERE o.payment IS NOT NULL AND o.payment.status = :paymentStatus)
		""")
	Long countContractsByUserIdAndNotPaymentStatus(@Param("userId") Long userId,
		@Param("statuses") List<MatchingStatus> statuses,
		@Param("paymentStatus") PaymentStatus paymentStatus);

	// 카운트 - 전문가(PRO) 기준, PaymentStatus.OK인 Order가 존재하는 매칭 수
	@Query("""
		    SELECT COUNT(DISTINCT m.id) FROM Matching m
		    JOIN m.suggestion sug
		    WHERE sug.proProfile.user.id = :userId
		      AND m.status IN :statuses
		      AND EXISTS (SELECT o FROM m.orders o WHERE o.payment IS NOT NULL AND o.payment.status = :paymentStatus)
		""")
	Long countContractsByProUserIdAndPaymentStatus(@Param("userId") Long userId,
		@Param("statuses") List<MatchingStatus> statuses,
		@Param("paymentStatus") PaymentStatus paymentStatus);

	// 카운트 - 전문가(PRO) 기준, PaymentStatus.OK인 Order가 없는 매칭 수 (= READY)
	@Query("""
		    SELECT COUNT(DISTINCT m.id) FROM Matching m
		    JOIN m.suggestion sug
		    WHERE sug.proProfile.user.id = :userId
		      AND m.status IN :statuses
		      AND NOT EXISTS (SELECT o FROM m.orders o WHERE o.payment IS NOT NULL AND o.payment.status = :paymentStatus)
		""")
	Long countContractsByProUserIdAndNotPaymentStatus(@Param("userId") Long userId,
		@Param("statuses") List<MatchingStatus> statuses,
		@Param("paymentStatus") PaymentStatus paymentStatus);

}
