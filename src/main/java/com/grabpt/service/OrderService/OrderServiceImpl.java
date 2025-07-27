package com.grabpt.service.OrderService;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grabpt.domain.entity.Order;
import com.grabpt.domain.entity.Payment;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.PaymentStatus;
import com.grabpt.repository.OrderRepository.OrderRepository;
import com.grabpt.repository.PaymentRepository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepository;
	private final PaymentRepository paymentRepository;

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
	public Order customOrder(Users user, Long price, String itemName) {
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
			.build();

		return orderRepository.save(order);
	}
}
