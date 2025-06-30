package com.grabpt.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.converter.TempConverter;
import com.grabpt.dto.TempResponse;
import com.grabpt.service.TempService.TempQueryService;

import lombok.RequiredArgsConstructor;

@RestController // view로 바로 출력
@RequestMapping("/temp")
@RequiredArgsConstructor
public class TempRestController {

	private final TempQueryService tempQueryService;

	@GetMapping("/test")
	public ApiResponse<TempResponse.TempTestDto> testApi() {
		return ApiResponse.onSuccess(TempConverter.toTempTestDto());
	}

	@GetMapping("/exception")
	public ApiResponse<TempResponse.TempExceptionDTO> exceptionAPI(@RequestParam Integer flag) {
		tempQueryService.CheckFlag(flag);
		return ApiResponse.onSuccess(TempConverter.toTempExceptionDto(flag));
	}

}
