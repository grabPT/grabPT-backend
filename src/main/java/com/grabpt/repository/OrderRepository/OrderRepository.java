package com.grabpt.repository.OrderRepository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.grabpt.domain.entity.Order;

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

}
