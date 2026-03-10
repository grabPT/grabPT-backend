package com.grabpt.repository.OrderRepository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grabpt.domain.entity.Order;
import com.grabpt.domain.enums.PaymentStatus;
import com.grabpt.dto.response.MemberPaymentDto;
import com.grabpt.dto.response.UserDashboardDto;

public interface OrderRepository extends JpaRepository<Order, Long> {

	@Query("select o from Order o" +
		" left join fetch o.payment p" +
		" left join fetch o.user m" +
		" where o.orderUid = :orderUid")
	Optional<Order> findOrderAndPaymentAndMember(String orderUid);

	@Query("select o from Order o" +
		" left join fetch o.payment p" +
		" where o.orderUid = :orderUid")
	Optional<Order> findOrderAndPayment(String orderUid);

	/** 트레이너 총 적립 금액 */
	@Query("SELECT COALESCE(SUM(o.price), 0) FROM Order o " +
		"JOIN o.matching m " +
		"JOIN m.suggestion s " +
		"WHERE s.proProfile.id = :proProfileId " +
		"AND o.payment.status = :status")
	Long getTrainerTotalEarnings(@Param("proProfileId") Long proProfileId,
		@Param("status") PaymentStatus status);

	/** 트레이너 총 결제 건수 */
	@Query("SELECT COUNT(o) FROM Order o " +
		"JOIN o.matching m " +
		"JOIN m.suggestion s " +
		"WHERE s.proProfile.id = :proProfileId " +
		"AND o.payment.status = :status")
	Long getTrainerTotalOrders(@Param("proProfileId") Long proProfileId,
		@Param("status") PaymentStatus status);

	/** 회원 결제 내역 (페이징) */
	@Query("SELECT new com.grabpt.dto.response.MemberPaymentDto(" +
		"c.id, " +
		"u.nickname, " +
		"r.sessionCount, " +
		"o.price, " +
		"o.price, " +
		"p.createdAt) " +
		"FROM Order o " +
		"JOIN o.payment p " +
		"JOIN o.matching m " +
		"JOIN m.suggestion s " +
		"JOIN m.requestion r " +
		"JOIN m.contract c " +
		"JOIN r.user u " +
		"WHERE s.proProfile.id = :proProfileId " +
		"AND p.status = :status " +
		"ORDER BY p.createdAt DESC")
	Page<MemberPaymentDto> getMemberPayments(@Param("proProfileId") Long proProfileId,
		@Param("status") PaymentStatus status,
		Pageable pageable);

	/** 회원: 총 결제 금액 */
	@Query("""
		    SELECT COALESCE(SUM(o.price), 0)
		    FROM Order o
		    WHERE o.user.id = :userId
		      AND o.payment.status = :status
		""")
	Long getUserTotalSpent(@Param("userId") Long userId,
		@Param("status") PaymentStatus status);

	/** 회원: 총 결제 건수 */
	@Query("""
		    SELECT COUNT(o)
		    FROM Order o
		    WHERE o.user.id = :userId
		      AND o.payment.status = :status
		""")
	Long getUserTotalOrders(@Param("userId") Long userId,
		@Param("status") PaymentStatus status);

	/**
	 * 회원: 결제 내역 (페이징) → UserDashboardDto
	 * - countQuery를 명시해서 페이징/카운트 불일치 문제 방지
	 */
	@Query(
		value = """
			    SELECT new com.grabpt.dto.response.UserDashboardDto(
			        c.id,
			        trainerUser.nickname,
			        r.sessionCount,
			        o.price,
			        p.createdAt
			    )
			    FROM Order o
			      JOIN o.payment p
			      JOIN o.matching m
			      LEFT JOIN m.contract c
			      JOIN m.suggestion s
			      JOIN s.proProfile pp
			      JOIN pp.user trainerUser
			      JOIN m.requestion r
			    WHERE o.user.id = :userId
			      AND p.status = :status
			    ORDER BY p.createdAt DESC
			""",
		countQuery = """
			    SELECT COUNT(o)
			    FROM Order o
			      JOIN o.payment p
			      JOIN o.matching m
			    WHERE o.user.id = :userId
			      AND p.status = :status
			"""
	)
	Page<UserDashboardDto> getUserPayments(@Param("userId") Long userId,
		@Param("status") PaymentStatus status,
		Pageable pageable);
}
