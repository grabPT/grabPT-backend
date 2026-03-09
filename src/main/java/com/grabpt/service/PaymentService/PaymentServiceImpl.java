package com.grabpt.service.PaymentService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

	private final OrderService orderService;
	private final PaymentRepository paymentRepository;
	private final IamportClient iamportClient;
	private final AlarmService alarmService;

	@Value("${import.api.key}")
	private String impApiKey;

	@Value("${import.api.secret}")
	private String impApiSecret;

	/**
	 * PortOne V1 결제 단건 조회 (include_sandbox=true 포함)
	 * 2026-01-26 이후 테스트 채널 결제는 기본적으로 404를 반환하므로 직접 API 호출
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> getPaymentIncludingSandbox(String impUid) {
		RestTemplate restTemplate = new RestTemplate();

		// 1. 액세스 토큰 발급
		HttpHeaders tokenHeaders = new HttpHeaders();
		tokenHeaders.setContentType(MediaType.APPLICATION_JSON);
		Map<String, String> tokenBody = Map.of("imp_key", impApiKey, "imp_secret", impApiSecret);
		ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
			"https://api.iamport.kr/users/getToken",
			new HttpEntity<>(tokenBody, tokenHeaders),
			Map.class
		);
		Map<String, Object> tokenData = (Map<String, Object>) tokenResponse.getBody().get("response");
		String accessToken = (String) tokenData.get("access_token");

		// 2. include_sandbox=true 로 결제 단건 조회
		HttpHeaders paymentHeaders = new HttpHeaders();
		paymentHeaders.setBearerAuth(accessToken);
		ResponseEntity<Map> paymentResponse = restTemplate.exchange(
			"https://api.iamport.kr/payments/" + impUid + "?include_sandbox=true",
			HttpMethod.GET,
			new HttpEntity<>(paymentHeaders),
			Map.class
		);

		Map<String, Object> body = paymentResponse.getBody();
		if (body == null || !Integer.valueOf(0).equals(body.get("code"))) {
			throw new RuntimeException("결제 조회 실패: " + (body != null ? body.get("message") : "응답 없음"));
		}

		Map<String, Object> paymentData = (Map<String, Object>) body.get("response");
		if (paymentData == null) {
			throw new RuntimeException("존재하지 않는 결제정보입니다.");
		}

		return paymentData;
	}

	@Override // 결제 정보 확인 및 검증
	public IamportResponse<Payment> paymentByCallback(ImPortRequestDto.PaymentCallbackRequest request) {
		try {
			Map<String, Object> payment = getPaymentIncludingSandbox(request.getPayment_uid());

			// 주문내역 조회
			Order order = orderService.findOrderAndPayment(request.getOrder_uid())
				.orElseThrow(() -> new IllegalArgumentException("주문 내역이 없습니다."));

			String status = (String) payment.get("status");
			int iamportPrice = ((Number) payment.get("amount")).intValue();
			String impUid = (String) payment.get("imp_uid");

			// 결제 완료가 아니면
			if (!"paid".equals(status)) {
				orderService.delete(order);
				paymentRepository.delete(order.getPayment());
				throw new RuntimeException("결제 미완료");
			}

			// 결제 금액 검증
			Long price = order.getPayment().getPrice();
			if (iamportPrice != price) {
				orderService.delete(order);
				paymentRepository.delete(order.getPayment());
				iamportClient.cancelPaymentByImpUid(
					new CancelData(impUid, true, new BigDecimal(iamportPrice)));
				throw new RuntimeException("결제금액 위변조 의심");
			}

			// 결제 상태 변경
			order.getPayment().changePaymentBySuccess(PaymentStatus.OK, impUid);

			try {
				Long contractId = order.getMatching().getContract().getId();
				Long proId = order.getMatching().getSuggestion().getProProfile().getUser().getId();
				alarmService.sendAlarm(proId, "SUCCESS", "결제 완료",
					"결제가 성공적으로 완료되었습니다.", "/contracts/" + contractId);
			} catch (Exception e) {
				log.warn("결제 완료 알람 전송 실패 (결제 상태는 OK로 저장됨): {}", e.getMessage());
			}

			// 라이브러리 응답 객체가 필요한 경우 기존 방식으로 조회 (로깅용)
			return iamportClient.paymentByImpUid(request.getPayment_uid());

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

		String address = (order.getUser().getAddress() != null) ? order.getUser().getAddress().getStreet() : null;
		String zipcode = (order.getUser().getAddress() != null) ? order.getUser().getAddress().getZipcode() : null;

		return ImPortRequestDto.CustomRequestPayDto.builder()
			.buyer_name(order.getUser().getUsername())
			.buyer_email(order.getUser().getEmail())
			.buyer_address(address)
			.payment_price(order.getPayment().getPrice())
			.item_name(order.getItemName())
			.buyer_tel(order.getUser().getPhone_number())
			.buyer_postcode(zipcode)
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
		log.info("[Payment] paymentCallback 수신 - payment_uid={}, order_uid={}", request.getPayment_uid(),
			request.getOrder_uid());
		try {
			Map<String, Object> payment = getPaymentIncludingSandbox(request.getPayment_uid());

			// 주문내역 조회
			Order order = orderService.findOrderAndPayment(request.getOrder_uid())
				.orElseThrow(() -> new IllegalArgumentException("주문 내역이 없습니다."));

			String status = (String) payment.get("status");
			int iamportPrice = ((Number) payment.get("amount")).intValue();
			String impUid = (String) payment.get("imp_uid");

			// 결제 완료가 아니면
			if (!"paid".equals(status)) {
				orderService.delete(order);
				paymentRepository.delete(order.getPayment());
				throw new RuntimeException("결제 미완료");
			}

			// 결제 금액 검증
			Long price = order.getPayment().getPrice();
			if (iamportPrice != price) {
				orderService.delete(order);
				paymentRepository.delete(order.getPayment());
				iamportClient.cancelPaymentByImpUid(
					new CancelData(impUid, true, new BigDecimal(iamportPrice)));
				throw new RuntimeException("결제금액 위변조 의심");
			}

			// 결제 상태 변경
			order.getPayment().changePaymentBySuccess(PaymentStatus.OK, impUid);

			try {
				Long contractId = order.getMatching().getContract().getId();
				Long proId = order.getMatching().getSuggestion().getProProfile().getUser().getId();
				alarmService.sendAlarm(proId, "SUCCESS", "결제 완료",
					"결제가 성공적으로 완료되었습니다.", "/contracts/" + contractId);
			} catch (Exception e) {
				log.warn("결제 완료 알람 전송 실패 (결제 상태는 OK로 저장됨): {}", e.getMessage());
			}

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
