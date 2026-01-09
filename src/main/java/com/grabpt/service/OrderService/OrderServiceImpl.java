package com.grabpt.service.OrderService;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grabpt.domain.entity.Matching;
import com.grabpt.domain.entity.Order;
import com.grabpt.domain.entity.Payment;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.PaymentStatus;
import com.grabpt.dto.response.MemberPaymentDto;
import com.grabpt.dto.response.UserDashboardDto;
import com.grabpt.repository.OrderRepository.OrderRepository;
import com.grabpt.repository.PaymentRepository.PaymentRepository;
import com.grabpt.service.MatchingService.MatchingService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepository;
	private final PaymentRepository paymentRepository;
	private final MatchingService matchingService;

	@Override
	public Order order(Users users) {
		// 임시 결제내역 생성 (제공받은 Payment를 사용할 예정, 그치만 테스트용으로 만들었다)
		Payment testPayment = Payment.builder()
			.price(100L)
			.status(PaymentStatus.READY)
			.build();

		paymentRepository.save(testPayment);

		// 주문 생성 (제공받은 Order을 사용할 예정, 테스트용으로 만들었다)
		Order testOrder = Order.builder()
			.user(users)
			.price(testPayment.getPrice())
			.itemName("grabPT 테스트용 결제")
			.orderUid(UUID.randomUUID().toString())
			.payment(testPayment)
			.build();

		return orderRepository.save(testOrder);
	}

	@Override
	public Order customOrder(Users user, Long price, String itemName, Long matchingId) {

		// 매칭 조회
		Matching matching = matchingService.findById(matchingId)
			.orElseThrow(() -> new IllegalArgumentException("Matching not found with id: " + matchingId));
		// 결제내역 생성
		Payment payment = Payment.builder()
			.price(price)
			.status(PaymentStatus.READY)
			.build();

		paymentRepository.save(payment);

		// 주문 생성
		Order order = Order.builder()
			.user(user)
			.price(price)
			.itemName(itemName)
			.orderUid(UUID.randomUUID().toString())
			.payment(payment)
			.matching(matching)
			.build();

		return orderRepository.save(order);
	}

	@Override
	public Optional<Order> findOrderAndPayment(String orderUid) {
		return orderRepository.findOrderAndPayment(orderUid);
	}

	@Override
	public void delete(Order order) {
		orderRepository.delete(order);
	}

	@Override
	public Optional<Order> findOrderAndPaymentAndMember(String orderUid) {
		return orderRepository.findOrderAndPaymentAndMember(orderUid);
	}

	@Override
	public Long getTrainerTotalEarnings(Long proProfileId, PaymentStatus paymentStatus) {
		return orderRepository.getTrainerTotalEarnings(proProfileId, paymentStatus);
	}

	@Override
	public Long getTrainerTotalOrders(Long proProfileId, PaymentStatus paymentStatus) {
		return orderRepository.getTrainerTotalOrders(proProfileId, paymentStatus);
	}

	@Override
	public Page<MemberPaymentDto> getMemberPayments(Long proProfileId, PaymentStatus paymentStatus, Pageable pageable) {
		return orderRepository.getMemberPayments(proProfileId, paymentStatus, pageable);
	}

	@Override
	public Long getUserTotalSpent(Long userId, PaymentStatus paymentStatus) {
		return orderRepository.getUserTotalSpent(userId, paymentStatus);
	}

	@Override
	public Long getUserTotalOrders(Long userId, PaymentStatus paymentStatus) {
		return orderRepository.getUserTotalOrders(userId, paymentStatus);
	}

	@Override
	public Page<UserDashboardDto> getUserPayments(Long userId, PaymentStatus paymentStatus, Pageable pageable) {
		return orderRepository.getUserPayments(userId, paymentStatus, pageable);
	}
}
