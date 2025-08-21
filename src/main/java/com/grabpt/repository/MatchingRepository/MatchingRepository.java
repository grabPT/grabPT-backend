package com.grabpt.repository.MatchingRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grabpt.domain.entity.Matching;
import com.grabpt.domain.enums.MatchingStatus;

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

}
