package com.grabpt.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.grabpt.dto.request.ImPortRequestDto;
import com.grabpt.service.PaymentService.PaymentService;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PaymentController {
	private final PaymentService paymentService;

	@GetMapping("/payment/{id}") // view로 전달할 결제 관련 데이터
	public String paymentPage(@PathVariable(name = "id", required = false) String orderUid, Model model) {

		ImPortRequestDto.CustomRequestPayDto requestDto = paymentService.findCustomRequestDto(orderUid);
		log.info("requestDto = {}", requestDto);

		if (requestDto == null) {
			throw new IllegalArgumentException("결제 요청 데이터를 찾을 수 없습니다: " + orderUid);
		}
		model.addAttribute("requestDto", requestDto);

		return "payment";
	}

	@ResponseBody
	@PostMapping("/payment")
	public ResponseEntity<IamportResponse<Payment>> validationPayment(
		@RequestBody ImPortRequestDto.PaymentCallbackRequest request) {
		IamportResponse<Payment> iamportResponse = paymentService.paymentByCallback(request);

		log.info("결제 응답={}", iamportResponse.getResponse().toString());

		return new ResponseEntity<>(iamportResponse, HttpStatus.OK);
	}

	@GetMapping("/success-payment")
	public String successPaymentPage() {
		return "success-payment";
	}

	@GetMapping("/fail-payment")
	public String failPaymentPage() {
		return "fail-payment";
	}
}
