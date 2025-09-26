package com.grabpt.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.domain.entity.Order;
import com.grabpt.domain.entity.Users;
import com.grabpt.dto.request.ImPortRequestDto;
import com.grabpt.service.OrderService.OrderService;
import com.grabpt.service.PaymentService.PaymentService;
import com.grabpt.service.UserService.UserQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class OrderController {

	private final UserQueryService userQueryService;
	private final OrderService orderService;
	private final PaymentService paymentService;

	@GetMapping("/order")
	public String order(@RequestParam(name = "message", required = false) String message,
		@RequestParam(name = "orderUid", required = false) String id,
		Model model) {

		model.addAttribute("message", message);
		model.addAttribute("orderUid", id);

		return "order";
	}

	@PostMapping("/order")
	public String autoOrder(HttpServletRequest request, ImPortRequestDto.CustomOrderRequestDto req) throws
		IllegalAccessException {
		// String userEmail = userQueryService.getUserInfo(request).getEmail();
		// 추후 JWT Access토큰으로 사용자 정보 인증 필요
		Users user = userQueryService.findByEmail("email@gmail.com").get();
		Order order = orderService.order(user);

		String message = "주문 실패";
		if (order != null) {
			message = "주문 성공";
		}

		String encode = URLEncoder.encode(message, StandardCharsets.UTF_8);

		return "redirect:/order?message=" + encode + "&orderUid=" + order.getOrderUid();
	}

	// JWT AccessToken을 받아 사용자 정보를 저장하는 로직
	// 계산 시 정보 추가한 버전
	@ResponseBody
	@PostMapping(value = "/customOrder", consumes = "application/json", produces = "application/json")
	@Operation(
		summary = "사용자 정보를 받아 주문 저장 후 결제요청 DTO 반환",
		description = """
			## 사용 목적 및 기능 요약
			- 클라이언트의 사용자 토큰으로 사용자 정보를 확인한 뒤 주문을 생성합니다.
			- 생성된 주문으로 **결제 페이지 요청 DTO**를 구성해 반환합니다.
			""",
		security = {@SecurityRequirement(name = "JWT TOKEN")}
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "주문 생성 및 결제요청 DTO 생성 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ImPortRequestDto.CustomRequestPayDto.class),
				examples = @ExampleObject(
					name = "성공 예시",
					value = """
						{
						  "isSuccess": true,
						  "code": "OK",
						  "message": "요청에 성공했습니다.",
						  "result": {
						    "orderUid": "order_20250926_0001",
						    "item_name": "1:1 PT 10회권",
						    "buyer_email": "email@gmail.com",
						    "buyer_name": "홍길동",
						    "amount": 300000,
						    "merchant_uid": "order_20250926_0001",
						    "buyer_tel": "010-1234-5678",
						    "redirect_url": "/payment/order_20250926_0001"
						  }
						}
						"""
				)
			)
		),
	})
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
		required = true,
		description = """
			## Request 내용 설명
			- price(integer): 결제 금액(원)
			- item_name(string): 상품명
			- matching_id(long): 매칭 ID (결제 연동용)
			""",
		content = @Content(
			schema = @Schema(implementation = ImPortRequestDto.CustomOrderRequestDto.class),
			examples = @ExampleObject(
				name = "요청 예시",
				value = """
					{
					  "price": 300000,
					  "item_name": "강프로님의 1:1 PT 10회권",
					  "matching_id": 101
					}
					"""
			)
		)
	)
	public ApiResponse<ImPortRequestDto.CustomRequestPayDto> customOrder(HttpServletRequest request,
		@RequestBody ImPortRequestDto.CustomOrderRequestDto req) throws
		IllegalAccessException {

		String email = userQueryService.getUserInfo(request).getEmail();
		Users user = userQueryService.findByEmail(email)
			.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		// 1) 주문 생성
		Order order = orderService.customOrder(user, req.getPrice(), req.getItem_name(), req.getMatching_id());

		// 2) 결제 페이지 DTO 구성
		ImPortRequestDto.CustomRequestPayDto requestDto = paymentService.buildCustomRequestPayDto(order);

		// 4) 바로 결제 페이지 렌더
		return ApiResponse.onSuccess(requestDto);
	}
}
