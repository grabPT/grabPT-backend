package com.grabpt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.ProfileHandler;
import com.grabpt.apiPayload.exception.handler.UserHandler;
import com.grabpt.domain.entity.ProProfile;
import com.grabpt.domain.entity.Users;
import com.grabpt.dto.response.TrainerDashboardDto;
import com.grabpt.dto.response.UserResponseDto;
import com.grabpt.service.SettlementService.SettlementService;
import com.grabpt.service.UserService.UserQueryService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
public class SettlementController {

	private final UserQueryService userQueryService;
	private final SettlementService settlementService;

	@GetMapping("/trainer/dashboard")
	@Operation(summary = "트레이너 정산 대시보드",
		description = "트레이너의 적립 금액, 결제 건수, 활성 회원, 회원 결제 내역 조회")
	public ApiResponse<TrainerDashboardDto> getTrainerDashboard(HttpServletRequest request,
		@RequestParam(defaultValue = "1") int page) throws IllegalAccessException {
		UserResponseDto.UserInfoDTO userInfo = userQueryService.getUserInfo(request);
		String email = userInfo.getEmail();
		Users trainer = userQueryService.findByEmail(email)
			.orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));

		ProProfile proProfile = trainer.getProProfile();
		if (proProfile == null) {
			throw new ProfileHandler(ErrorStatus.PROFILE_NOT_FOUND);
		}

		TrainerDashboardDto dashboard = settlementService.getTrainerDashboard(proProfile.getId(),
			page - 1, 5);
		return ApiResponse.onSuccess(dashboard);
	}

}
