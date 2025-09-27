package com.grabpt.controller;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.AlarmHandler;
import com.grabpt.config.SecurityUtils;
import com.grabpt.converter.AlarmConverter;
import com.grabpt.domain.entity.Alarm;
import com.grabpt.dto.response.AlarmResponseDto;
import com.grabpt.dto.response.CategoryResponse;
import com.grabpt.dto.response.UserResponseDto;
import com.grabpt.repository.AlarmRepository.AlarmRepository;
import com.grabpt.service.AlarmService.AlarmService;
import com.grabpt.service.UserService.UserQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AlarmController {

	private final AlarmService alarmService;
	private final UserQueryService userQueryService;

	@Operation(summary = "로그인 유저의 읽지 않은 알림을 모두 조회합니다")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "읽지 않은 알림 목록 조회 성공",
			content = @Content(mediaType = "application/json",
				schema = @Schema(implementation = AlarmResponseDto.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰을 넣어주세요")
	})
	@GetMapping("/api/alarmList")
	@ResponseBody
	public ApiResponse<List<AlarmResponseDto>> getAlarmList(){
		Long userId = SecurityUtils.currentUserIdOrThrow();
		List<Alarm> alarmList = alarmService.findAllUnReadAlarmByUserId(userId);
		List<AlarmResponseDto> list = alarmList.stream().map(AlarmConverter::toAlarmResponseDto).toList();
		return ApiResponse.onSuccess(list);
	}

	@Operation(summary = "알림 읽음 처리 API")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "알림 읽음 처리 성공",
			content = @Content(mediaType = "application/json",
				schema = @Schema(implementation = AlarmResponseDto.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰을 넣어주세요")
	})
	@PatchMapping("/api/alarm/{alarmId}/read")
	@ResponseBody
	public ApiResponse<AlarmResponseDto> readAlarm(@PathVariable(name = "alarmId") Long alarmId){
		return ApiResponse.onSuccess(alarmService.readAlarm(alarmId));
	}
}
