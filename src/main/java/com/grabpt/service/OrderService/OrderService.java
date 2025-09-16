package com.grabpt.service.OrderService;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.grabpt.domain.entity.Order;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.PaymentStatus;
import com.grabpt.dto.response.MemberPaymentDto;
import com.grabpt.dto.response.UserDashboardDto;

public interface OrderService {
	// 주문 정보 저장 (사용자 정보와 프론트에서 제공해준 Payment를 입력받는다)
	Order order(Users users);

	Order customOrder(Users user, Long price, String itemName, Long matchingId);

	Optional<Object> findOrderAndPayment(String orderUid);

	void delete(Order order);

	Optional<Object> findOrderAndPaymentAndMember(String orderUid);

	Long getTrainerTotalEarnings(Long proProfileId, PaymentStatus paymentStatus);

	Long getTrainerTotalOrders(Long proProfileId, PaymentStatus paymentStatus);

	Page<MemberPaymentDto> getMemberPayments(Long proProfileId, PaymentStatus paymentStatus, Pageable pageable);

	Long getUserTotalSpent(Long userId, PaymentStatus paymentStatus);

	Long getUserTotalOrders(Long userId, PaymentStatus paymentStatus);

	Page<UserDashboardDto> getUserPayments(Long userId, PaymentStatus paymentStatus, Pageable pageable);

}
