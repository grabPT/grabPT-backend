package com.grabpt.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.GeneralException;
import com.grabpt.config.SecurityUtils;
import com.grabpt.config.auth.PrincipalDetails;
import com.grabpt.dto.request.DeletedRequestDTO;
import com.grabpt.dto.request.UserProfileUpdateRequestDTO;
import com.grabpt.dto.response.MyRequestListDTO;
import com.grabpt.dto.response.MyReviewListDTO;
import com.grabpt.dto.response.ProfileResponseDTO;
import com.grabpt.service.ProfileService.ProfileService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyProfileController {

	private final ProfileService profileService;
	private final ObjectMapper objectMapper;

	@Operation(
		description = "유저의 프로필을 조회합니다.",
		summary = "유저의 프로필을 조회합니다."
	)
	@GetMapping
	public ApiResponse<ProfileResponseDTO.MyProfileDTO> getMyUserProfile() {
		Long userId = SecurityUtils.currentUserIdOrThrow();
		return ApiResponse.onSuccess(profileService.findMyUserProfile(userId));
	}

	@PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "기본 프로필 수정", description = "{ \"nickname\": \"test\", \"address\": { \"city\": \"test\", \"district\": \"test\", \"street\": \"test\", \"zipcode\": \"test\", \"specAddress\" : \"test\"  } }")
	public ApiResponse<String> updateMyUserProfile(
		@RequestParam("request") String requestJson,
		@RequestPart(value = "image", required = false) MultipartFile profileImage) { // 이미지는 선택사항으로 처리

		Long userId = SecurityUtils.currentUserIdOrThrow();
		UserProfileUpdateRequestDTO request;

		try {
			request = objectMapper.readValue(requestJson, UserProfileUpdateRequestDTO.class);
		} catch (JsonProcessingException e) {

			throw new GeneralException(ErrorStatus._BAD_REQUEST);
		}

		profileService.updateMyUserProfile(userId, request, profileImage);

		return ApiResponse.onSuccess("프로필이 성공적으로 수정되었습니다.");
	}

	@GetMapping("/reviews")
	@Operation(summary = "리뷰(review) 확인 API")
	public ApiResponse<Page<MyReviewListDTO>> getMyReviewList(
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size){

		Long userId = SecurityUtils.currentUserIdOrThrow();

		Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
		return ApiResponse.onSuccess(profileService.findMyReviews(userId, pageable));
	}

	@GetMapping("/requests")
	@Operation(summary = "요청서(request) 확인 API")
	public ApiResponse<Page<MyRequestListDTO>> getMyRequestList(
		HttpServletRequest request,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size)  {

		Long userId = SecurityUtils.currentUserIdOrThrow();
		Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
		return ApiResponse.onSuccess(profileService.findMyRequests(userId, pageable));
	}

	@PatchMapping(value = "/image", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	@Operation(summary = "이미지(Image) 변경")
	public ApiResponse<String> updateUserProfileImage(
		HttpServletRequest request,
		@RequestPart(value = "image") MultipartFile profileImage)  {

		Long userId = SecurityUtils.currentUserIdOrThrow();
		profileService.updateUserProfileImage(userId, profileImage);

		return ApiResponse.onSuccess("프로필 이미지가 성공적으로 수정되었습니다.");
	}

	@DeleteMapping
	@Operation(summary = "회원 탈퇴 API", description = "현재 로그인된 사용자의 계정을 비활성화합니다.")
	public ApiResponse<String> withdrawUser(
		HttpServletRequest req,
		HttpServletResponse res,
		@RequestBody DeletedRequestDTO requestDto
	) {

		Authentication authentication = (Authentication) req.getUserPrincipal();
		PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
		Long userId = principalDetails.getUser().getId();

		profileService.deleteUser(userId, requestDto);


		log.info("사용자 데이터 삭제 완료. userId = {}", userId);

		log.info("세션 및 쿠키 작업을 진행합니다...");

		Cookie accessTokenCookie = new Cookie("access_token", null);
		accessTokenCookie.setMaxAge(0);
		accessTokenCookie.setPath("/");
		res.addCookie(accessTokenCookie);
		log.info("쿠키 삭제 -> access_token");

		Cookie refreshTokenCookie = new Cookie("refresh_token", null);
		refreshTokenCookie.setMaxAge(0);
		refreshTokenCookie.setPath("/");
		res.addCookie(refreshTokenCookie);
		log.info("쿠키 삭제 -> refresh_token");

		var session = req.getSession(false);
		if (session != null) {
			session.invalidate();
			log.info("HTTP 세션을 무효화합니다,");
		}
		SecurityContextHolder.clearContext();
		log.info("SecurityContextHolder를 정리했습니다.");

		log.info("회원 탈퇴 완료.");

		return ApiResponse.onSuccess("회원 탈퇴가 성공적으로 처리되었습니다.");
	}

	@PatchMapping(value = "/restore")
	@Operation(summary = "회원 복구")
	public ApiResponse<String> restoreUser() {
		Long userId = SecurityUtils.currentUserIdOrThrow();
		profileService.restoreUser(userId);
		return ApiResponse.onSuccess("회원 복구가 성공적으로 처리되었습니다.");
	}

}
