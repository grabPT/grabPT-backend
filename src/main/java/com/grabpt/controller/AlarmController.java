package com.grabpt.controller;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.converter.AlarmConverter;
import com.grabpt.domain.entity.Alarm;
import com.grabpt.dto.response.AlarmResponseDto;
import com.grabpt.dto.response.UserResponseDto;
import com.grabpt.repository.AlarmRepository.AlarmRepository;
import com.grabpt.service.UserService.UserQueryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AlarmController {

	private final UserQueryService userQueryService;
	private final AlarmRepository alarmRepository;

	@GetMapping("/api/alarm")
	@ResponseBody
	public ApiResponse<List<AlarmResponseDto>> getAlarmList(HttpServletRequest request) throws IllegalAccessException {
		Long userId = userQueryService.getUserId(request);
		List<Alarm> alarmList = alarmRepository.findAllByUserId(userId);
		List<AlarmResponseDto> list = alarmList.stream().map(AlarmConverter::toAlarmResponseDto).toList();
		return ApiResponse.onSuccess(list);
	}

	@GetMapping("/alarm-test")
	public String test(HttpServletRequest request) throws IllegalAccessException {
		Long userId = userQueryService.getUserId(request);
		return "alarm-test";
	}
}
