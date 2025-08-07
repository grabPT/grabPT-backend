package com.grabpt.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.domain.entity.Suggestions;
import com.grabpt.dto.request.SuggestionRequestDto;
import com.grabpt.dto.response.SuggestionResponseDto;
import com.grabpt.dto.response.UserResponseDto;
import com.grabpt.service.SuggestionService.SuggestionService;
import com.grabpt.service.UserService.UserQueryService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/suggestion")
@RequiredArgsConstructor
@Slf4j
public class SuggestionController {

	private final SuggestionService suggestionService;
	private final UserQueryService userQueryService;

	// @Operation(
	// 	summary = "제안서 저장 API",
	// 	description = "트레이너가 보낸 제안서를 저장합니다."
	// )
	// @PostMapping
	// public ApiResponse<SuggestionResponseDto.SuggestionSaveResponseDto> setSuggestion(
	// 	@RequestBody SuggestionRequestDto dto,
	// 	HttpServletRequest request) throws IllegalAccessException {
	//
	// 	UserResponseDto.UserInfoDTO userInfo = userQueryService.getUserInfo(request);
	// 	String email = userInfo.getEmail();  // 현재 로그인한 트레이너 이메일
	//
	// 	Suggestions saved = suggestionService.save(dto, email);
	//
	// 	return ApiResponse.onSuccess(
	// 		SuggestionResponseDto.SuggestionSaveResponseDto.builder()
	// 			.suggestionId(saved.getId())
	// 			.build()
	// 	);
	// }

	@Operation(
		summary = "제안서 저장 API (Multipart)",
		description = "트레이너가 보낸 제안서를 저장합니다. JSON + 이미지 리스트 형식으로 전송하세요."
	)
	@PostMapping(value = "/{requestionId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse<SuggestionResponseDto.SuggestionSaveResponseDto> setSuggestion(
		@PathVariable Long requestionId,
		@RequestPart("data") SuggestionRequestDto dto,
		@RequestPart(value = "photos", required = false) List<MultipartFile> photos,
		HttpServletRequest request) throws IllegalAccessException {

		UserResponseDto.UserInfoDTO userInfo = userQueryService.getUserInfo(request);
		String email = userInfo.getEmail();  // 현재 로그인한 트레이너 이메일

		Suggestions saved = suggestionService.save(dto, email, photos, requestionId);

		return ApiResponse.onSuccess(
			SuggestionResponseDto.SuggestionSaveResponseDto.builder()
				.suggestionId(saved.getId())
				.build()
		);
	}

	@GetMapping("/{suggestionId}")
	@Operation(
		summary = "제안서 상세 조회 API",
		description = "제안서 정보와 트레이너 프로필 정보를 조회합니다."
	)
	public ApiResponse<SuggestionResponseDto.SuggestionDetailResponseDto> getSuggestionDetail(
		@PathVariable Long suggestionId) {
		SuggestionResponseDto.SuggestionDetailResponseDto response = suggestionService.getDetail(suggestionId);
		return ApiResponse.onSuccess(response);
	}

	@GetMapping("/requestionList/{requestionId}")
	@Operation(
		summary = "요청서에 대한 제안서 목록 조회 API",
		description = "요청서 ID에 해당하는 제안서들을 6개씩 페이징하여 조회합니다."
	)
	public ApiResponse<Page<SuggestionResponseDto.SuggestionResponsePagingDto>> getSuggestionsByRequestion(
		@PathVariable Long requestionId,
		@RequestParam(defaultValue = "1") int page
	) {
		int adjustedPage = Math.max(page - 1, 0);
		Page<SuggestionResponseDto.SuggestionResponsePagingDto> result =
			suggestionService.getSuggestionsByRequestionId(requestionId, adjustedPage);
		return ApiResponse.onSuccess(result);
	}

	@GetMapping("/mySuggestions")
	@Operation(
		summary = "트레이너 제안서 목록 조회 API",
		description = "로그인한 트레이너가 작성한 제안서를 8개씩 페이징하여 조회합니다."
	)
	public ApiResponse<Page<SuggestionResponseDto.MySuggestionPagingDto>> getMySuggestions(
		HttpServletRequest request,
		@RequestParam(defaultValue = "1") int page
	) throws IllegalAccessException {
		Page<SuggestionResponseDto.MySuggestionPagingDto> response = suggestionService.getMySuggestions(request, page);
		return ApiResponse.onSuccess(response);
	}

	@PatchMapping("/{suggestionId}")
	@Operation(
		summary = "제안서 수정 API",
		description = "작성자가 본인의 제안서를 수정합니다."
	)
	public ApiResponse<String> updateSuggestion(
		@PathVariable Long suggestionId,
		@RequestBody SuggestionRequestDto dto,
		HttpServletRequest request
	) throws IllegalAccessException {

		UserResponseDto.UserInfoDTO userInfo = userQueryService.getUserInfo(request);
		String email = userInfo.getEmail();

		suggestionService.updateSuggestion(suggestionId, dto, email);
		return ApiResponse.onSuccess("제안서가 성공적으로 수정되었습니다.");
	}

	@DeleteMapping("/{suggestionId}")
	@Operation(
		summary = "제안서 삭제 API",
		description = "작성자가 본인의 제안서를 삭제합니다."
	)
	public ApiResponse<String> deleteSuggestion(
		@PathVariable Long suggestionId,
		HttpServletRequest request
	) throws IllegalAccessException {

		UserResponseDto.UserInfoDTO userInfo = userQueryService.getUserInfo(request);
		String email = userInfo.getEmail();

		suggestionService.deleteSuggestion(suggestionId, email);
		return ApiResponse.onSuccess("제안서가 성공적으로 삭제되었습니다.");
	}

	@GetMapping("/{suggestionId}/suggestion-can-edit")
	@Operation(summary = "제안서 수정 가능 여부 확인", description = "현재 로그인한 사용자가 제안서를 작성했는지 확인")
	public ApiResponse<Map<String, Boolean>> canEditSuggestion(
		@PathVariable Long suggestionId,
		HttpServletRequest request
	) throws IllegalAccessException {
		UserResponseDto.UserInfoDTO userInfo = userQueryService.getUserInfo(request);
		String email = userInfo.getEmail();

		boolean canEdit = suggestionService.canEditSuggestion(suggestionId, email);

		Map<String, Boolean> response = new HashMap<>();
		response.put("canEdit", canEdit);
		return ApiResponse.onSuccess(response);
	}

}
