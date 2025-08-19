package com.grabpt.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.domain.entity.Order;
import com.grabpt.domain.entity.Users;
import com.grabpt.dto.request.ImPortRequestDto;
import com.grabpt.service.OrderService.OrderService;
import com.grabpt.service.PaymentService.PaymentService;
import com.grabpt.service.UserService.UserQueryService;

import io.swagger.v3.oas.annotations.Operation;
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

	@GetMapping("/customOrder")
	public String customOrder(@RequestParam(name = "message", required = false) String message,
		@RequestParam(name = "orderUid", required = false) String id,
		Model model) {

		model.addAttribute("message", message);
		model.addAttribute("orderUid", id);

		return "order";
	}

	// JWT AccessToken을 받아 사용자 정보를 저장하는 로직
	// 계산 시 정보 추가한 버전
	@PostMapping("/customOrder")
	@Operation(summary = "사용자 정보를 받아 order 정보를 저장하는 API입니다.",
		description = "사용자 정보를 토큰으로 받아 order 정보를 저장하는 API입니다.",
		security = {@SecurityRequirement(name = "JWT TOKEN")}
	)
	public ApiResponse<ImPortRequestDto.CustomRequestPayDto> customOrder(HttpServletRequest request,
		@RequestBody ImPortRequestDto.CustomOrderRequestDto req, Model model) throws
		IllegalAccessException {

		// String userEmail = userQueryService.getUserInfo(request).getEmail();
		// Users user = userQueryService.findByEmail(userEmail).get();
		// log.info("price = " + req.getPrice());
		//
		// //  matchingId 전달
		// Order customOrder = orderService.customOrder(user, req.getPrice(), req.getItemName(), req.getMatchingId());
		//
		// String message = "주문 실패";
		// if (customOrder != null) {
		// 	message = "주문 성공";
		// }
		//
		// String encode = URLEncoder.encode(message, StandardCharsets.UTF_8);
		//
		// return "redirect:/order?message=" + encode + "&orderUid=" + customOrder.getOrderUid();
		String email = userQueryService.getUserInfo(request).getEmail();
		Users user = userQueryService.findByEmail(email)
			.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		// 1) 주문 생성
		Order order = orderService.customOrder(user, req.getPrice(), req.getItemName(), req.getMatchingId());

		// 2) 결제 페이지 DTO 구성
		ImPortRequestDto.CustomRequestPayDto requestDto = paymentService.buildCustomRequestPayDto(order);

		// 4) 바로 결제 페이지 렌더
		return ApiResponse.onSuccess(requestDto);
	}
}
