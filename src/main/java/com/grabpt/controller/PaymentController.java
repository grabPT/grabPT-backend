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

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.dto.request.ImPortRequestDto;
import com.grabpt.service.PaymentService.PaymentService;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PaymentController {
	private final PaymentService paymentService;

	@Operation(
		summary = "결제 페이지 조회",
		description = "orderUid로 결제 정보를 조회하여 결제 페이지(HTML View)를 반환합니다."
	)
	@GetMapping("/payment/{id}")
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

	@ResponseBody
	@PostMapping("/paymentCallback")
	@Operation(summary = "결제 정보를 받아 결제가 유효한지 검증합니다.",
		description = "paymentUid와 orderUid를 받아 현재 결제가 유효한 결제인지 true, false로 반환하는 API입니다.")
	public ApiResponse<String> validationPaymentBoolean(
		@RequestBody ImPortRequestDto.PaymentCallbackRequest request) {
		Boolean validationResult = paymentService.paymentByCallbackBoolean(request);

		log.info("결제 응답={}", validationResult.toString());

		return ApiResponse.onSuccess(validationResult.toString());
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
