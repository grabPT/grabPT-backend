package com.grabpt.service.PaymentService;

import java.io.IOException;
import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.grabpt.domain.entity.Order;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.PaymentStatus;
import com.grabpt.dto.request.ImPortRequestDto;
import com.grabpt.repository.PaymentRepository.PaymentRepository;
import com.grabpt.service.AlarmService.AlarmService;
import com.grabpt.service.OrderService.OrderService;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	private final OrderService orderService;
	private final PaymentRepository paymentRepository;
	private final IamportClient iamportClient;
	private final AlarmService alarmService;

	@Override // 결제 정보 확인 및 검증
	public IamportResponse<Payment> paymentByCallback(ImPortRequestDto.PaymentCallbackRequest request) {
		try {
			// 결제 단건 조회(아임포트)
			IamportResponse<com.siot.IamportRestClient.response.Payment> iamportResponse = iamportClient.paymentByImpUid(
				request.getPayment_uid());

			// 주문내역 조회
			Order order = orderService.findOrderAndPayment(request.getOrder_uid())
				.orElseThrow(() -> new IllegalArgumentException("주문 내역이 없습니다."));

			// 결제 완료가 아니면
			if (!iamportResponse.getResponse().getStatus().equals("paid")) {
				// 주문, 결제 삭제
				orderService.delete(order);
				paymentRepository.delete(order.getPayment());

				throw new RuntimeException("결제 미완료");
			}

			// DB에 저장된 결제 금액
			Long price = order.getPayment().getPrice();
			// 실 결제 금액
			int iamportPrice = iamportResponse.getResponse().getAmount().intValue();

			// 결제 금액 검증
			if (iamportPrice != price) {
				// 주문, 결제 삭제
				orderService.delete(order);
				paymentRepository.delete(order.getPayment());

				// 결제금액 위변조로 의심되는 결제금액을 취소(아임포트)
				iamportClient.cancelPaymentByImpUid(
					new CancelData(iamportResponse.getResponse().getImpUid(), true, new BigDecimal(iamportPrice)));

				throw new RuntimeException("결제금액 위변조 의심");
			}

			// 결제 상태 변경
			order.getPayment().changePaymentBySuccess(PaymentStatus.OK, iamportResponse.getResponse().getImpUid());

			Long contractId = order.getMatching().getContract().getId();
			Long proId = order.getMatching().getSuggestion().getProProfile().getUser().getId();

			alarmService.sendAlarm(proId, "SUCCESS", "결제 완료",
				"결제가 성공적으로 완료되었습니다.", "/contracts/" + contractId);
			return iamportResponse;

		} catch (IamportResponseException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ImPortRequestDto.RequestPayDto findRequestDto(String orderUid) {
		Order order = orderService.findOrderAndPaymentAndMember(orderUid)
			.orElseThrow(() -> new IllegalArgumentException("주문이 없습니다."));

		return ImPortRequestDto.RequestPayDto.builder()
			.buyerName(order.getUser().getUsername())
			.buyerEmail(order.getUser().getEmail())
			.buyerAddress(order.getUser().getAddress().getStreet())
			.paymentPrice(order.getPayment().getPrice())
			.itemName(order.getItemName())
			.orderUid(order.getOrderUid())
			.build();
	}

	@Override
	public ImPortRequestDto.CustomRequestPayDto findCustomRequestDto(String orderUid) {
		Order order = orderService.findOrderAndPaymentAndMember(orderUid)
			.orElseThrow(() -> new IllegalArgumentException("주문이 없습니다."));

		return ImPortRequestDto.CustomRequestPayDto.builder()
			.buyer_name(order.getUser().getUsername())
			.buyer_email(order.getUser().getEmail())
			.buyer_address(order.getUser().getAddress().getStreet())
			.payment_price(order.getPayment().getPrice())
			.item_name(order.getItemName())
			.buyer_tel(order.getUser().getPhone_number())
			.buyer_postcode(order.getUser().getAddress().getZipcode())
			.order_uid(order.getOrderUid())
			.build();
	}

	@Override
	public ImPortRequestDto.CustomRequestPayDto buildCustomRequestPayDto(Order order) {
		Users buyer = order.getUser();

		String buyerName = buyer.getNickname();
		String buyerEmail = buyer.getEmail();
		String buyerTel = (buyer.getUserProfile() != null) ? buyer.getPhone_number() : null;
		String buyerAddress = (buyer.getAddress() != null) ? buyer.getAddress().getFullAddress() : null;
		String buyerPostcode = (buyer.getAddress() != null) ? buyer.getAddress().getZipcode() : null;

		return ImPortRequestDto.CustomRequestPayDto.builder()
			.order_uid(order.getOrderUid())
			.item_name(order.getItemName())
			.payment_price(order.getPrice())
			.buyer_name(buyerName)
			.buyer_email(buyerEmail)
			.buyer_address(buyerAddress)
			.buyer_tel(buyerTel)
			.buyer_postcode(buyerPostcode)
			.build();
	}

	@Override
	public boolean paymentByCallbackBoolean(ImPortRequestDto.PaymentCallbackRequest request) {
		try {
			// 결제 단건 조회(아임포트)
			IamportResponse<com.siot.IamportRestClient.response.Payment> iamportResponse = iamportClient.paymentByImpUid(
				request.getPayment_uid());

			// 주문내역 조회
			Order order = orderService.findOrderAndPayment(request.getOrder_uid())
				.orElseThrow(() -> new IllegalArgumentException("주문 내역이 없습니다."));

			// 결제 완료가 아니면
			if (!iamportResponse.getResponse().getStatus().equals("paid")) {
				// 주문, 결제 삭제
				orderService.delete(order);
				paymentRepository.delete(order.getPayment());

				throw new RuntimeException("결제 미완료");
			}

			// DB에 저장된 결제 금액
			Long price = order.getPayment().getPrice();
			// 실 결제 금액
			int iamportPrice = iamportResponse.getResponse().getAmount().intValue();

			// 결제 금액 검증
			if (iamportPrice != price) {
				// 주문, 결제 삭제
				orderService.delete(order);
				paymentRepository.delete(order.getPayment());

				// 결제금액 위변조로 의심되는 결제금액을 취소(아임포트)
				iamportClient.cancelPaymentByImpUid(
					new CancelData(iamportResponse.getResponse().getImpUid(), true, new BigDecimal(iamportPrice)));

				throw new RuntimeException("결제금액 위변조 의심");
			}

			// 결제 상태 변경
			order.getPayment().changePaymentBySuccess(PaymentStatus.OK, iamportResponse.getResponse().getImpUid());

			Long contractId = order.getMatching().getContract().getId();
			Long proId = order.getMatching().getSuggestion().getProProfile().getUser().getId();

			alarmService.sendAlarm(proId, "SUCCESS", "결제 완료",
				"결제가 성공적으로 완료되었습니다.", "/contracts/" + contractId);
			return true;

		} catch (IamportResponseException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void save(com.grabpt.domain.entity.Payment testPayment) {
		paymentRepository.save(testPayment);
	}
}
