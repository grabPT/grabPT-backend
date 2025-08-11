package com.grabpt.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.GeneralException;
import com.grabpt.dto.request.CertificationUpdateRequestDTO;
import com.grabpt.dto.request.UserProfileUpdateRequestDTO;
import com.grabpt.dto.response.MyRequestListDTO;
import com.grabpt.dto.response.MyReviewListDTO;
import com.grabpt.dto.response.ProfileResponseDTO;
import com.grabpt.service.ProfileService.ProfileService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyProfileController {

	private final ProfileService profileService;
	private final ObjectMapper objectMapper;

	// @AuthenticationPrincipal Long userId
	@Operation(
		description = "유저의 프로필을 조회합니다.",
		summary = "유저의 프로필을 조회합니다."
	)
	@GetMapping
	public ApiResponse<ProfileResponseDTO.MyProfileDTO> getMyUserProfile(@AuthenticationPrincipal(expression = "user.id") Long userId) {
		return ApiResponse.onSuccess(profileService.findMyUserProfile(userId));
	}

	@PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "기본 프로필 수정", description = "{\n"
		+ "  \"nickname\": \"string\",\n"
		+ "  \"address\": {\n"
		+ "    \"city\": \"string\",\n"
		+ "    \"district\": \"string\",\n"
		+ "    \"street\": \"string\",\n"
		+ "    \"zipcode\": \"string\"\n"
		+ "  }\n"
		+ "}")
	public ApiResponse<String> updateMyUserProfile(
		@AuthenticationPrincipal(expression = "user.id") Long userId,
		@RequestParam("request") String requestJson,
		@RequestPart(value = "image", required = false) MultipartFile profileImage) { // 이미지는 선택사항으로 처리

		// JSON 문자열을 DTO 객체로 변환
		UserProfileUpdateRequestDTO request;

		try {
			// JSON 문자열을 DTO 객체로 변환
			request = objectMapper.readValue(requestJson, UserProfileUpdateRequestDTO.class);
		} catch (JsonProcessingException e) {
			// JSON 파싱 실패 시, 400 Bad Request 에러를 발생시킵니다.
			throw new GeneralException(ErrorStatus._BAD_REQUEST);
		}

		profileService.updateMyUserProfile(userId, request, profileImage);

		return ApiResponse.onSuccess("프로필이 성공적으로 수정되었습니다.");
	}

	@GetMapping("/reviews")
	public ApiResponse<Page<MyReviewListDTO>> getMyReviewList(
		@AuthenticationPrincipal(expression = "user.id") Long userId,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size) {

		Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
		return ApiResponse.onSuccess(profileService.findMyReviews(userId, pageable));
	}

	@GetMapping("/requests")
	public ApiResponse<Page<MyRequestListDTO>> getMyRequestList(
		@AuthenticationPrincipal(expression = "user.id") Long userId,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size) {

		Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
		return ApiResponse.onSuccess(profileService.findMyRequests(userId, pageable));
	}

	@PatchMapping(value = "/image", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	public ApiResponse<String> updateUserProfileImage(
		@AuthenticationPrincipal(expression = "user.id") Long userId,
		@RequestPart(value = "image") MultipartFile profileImage) {

		profileService.updateUserProfileImage(userId, profileImage);

		return ApiResponse.onSuccess("프로필 이미지가 성공적으로 수정되었습니다.");
	}

	@DeleteMapping
	@Operation(summary = "회원 탈퇴 API", description = "현재 로그인된 사용자의 계정을 비활성화합니다.")
	public ApiResponse<String> withdrawUser(@AuthenticationPrincipal(expression = "user.id") Long userId) {
		profileService.deleteUser(userId);
		return ApiResponse.onSuccess("회원 탈퇴가 성공적으로 처리되었습니다.");
	}
}
