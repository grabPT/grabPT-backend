package com.grabpt.controller;

import java.util.List;

import com.grabpt.config.SecurityUtils;
import com.grabpt.dto.request.*;
import com.grabpt.service.ProfileService.ProfileFacade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.dto.response.CertificationResponseDTO;
import com.grabpt.dto.response.MyReviewListDTO;
import com.grabpt.dto.response.ProfileResponseDTO;
import com.grabpt.service.ProfileService.ProfileService;
import com.grabpt.service.UserService.UserQueryService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/mypage/pro")
@RequiredArgsConstructor
public class MyProPageController {

	private final ProfileFacade profileFacade;
	private final ObjectMapper objectMapper;
	private final UserQueryService userQueryService;

	@GetMapping
	@Operation(summary = "내 전문가 프로필을 조회합니다")
	public ApiResponse<ProfileResponseDTO.MyProProfileDTO> getMyProUserProfile()  {

		Long userId = SecurityUtils.currentUserIdOrThrow();
		return ApiResponse.onSuccess(profileFacade.findMyProUserProfile(userId));
	}

	@GetMapping("/reviews")
	@Operation(summary = "나(전문가) 한테 달린 리뷰를 확인합니다.")
	public ApiResponse<Page<MyReviewListDTO>> getProReviews(
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size){

		Long userId = SecurityUtils.currentUserIdOrThrow();
		Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
		Page<MyReviewListDTO> reviews = profileFacade.findProReviews(userId, pageable);
		return ApiResponse.onSuccess(reviews);
	}

	@GetMapping("/certification")
	@Operation(summary = "전문가 자격증/이력 조회 API")
	public ApiResponse<CertificationResponseDTO> getProCertifications(
		) {
		Long userId = SecurityUtils.currentUserIdOrThrow();
		CertificationResponseDTO certifications = profileFacade.findMyCertifications(userId);
		return ApiResponse.onSuccess(certifications);
	}

	@PostMapping(value = "/certification", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	@Operation(summary = "전문가 자격증/이력 등록 API", description = "{\n"
		+ "  \"existingCertifications\": [\n"
		+ "    {\n"
		+ "      \"imageUrl\": \"https://s3.bucket/path/to/existing-image.jpg\",\n"
		+ "      \"description\": \"수정된 설명\",\n"
		+ "      \"certificationType\": 0\n"
		+ "    }\n"
		+ "  ],\n"
		+ "  \"newCertifications\": [\n"
		+ "    {\n"
		+ "      \"description\": \"새로 추가하는 자격증\",\n"
		+ "      \"certificationType\": 1\n"
		+ "    }\n"
		+ "  ]\n"
		+ "}")
	public ApiResponse<String> registerProCertifications(
		@RequestParam("request") String requestJson, // DTO를 String으로 받음
		@RequestPart(value = "newImages", required = false) List<MultipartFile> newImages) throws Exception {

		Long userId = SecurityUtils.currentUserIdOrThrow();
		// JSON 문자열을 DTO 객체로 변환
		CertificationUpdateRequestDTO request = objectMapper.readValue(requestJson, CertificationUpdateRequestDTO.class);

		profileFacade.updateProCertifications(userId, request, newImages);
		return ApiResponse.onSuccess("자격증 정보가 성공적으로 등록되었습니다.");
	}

	@PatchMapping("/center")
	@Operation(summary = "센터명을 수정합니다")
	public ApiResponse<String> updateProCenter(
		@Valid @RequestBody CenterUpdateRequestDTO request){

		Long userId = SecurityUtils.currentUserIdOrThrow();
		profileFacade.updateProCenter(userId, request);
		return ApiResponse.onSuccess("센터 정보 수정이 완료되었습니다.");
	}

	@PatchMapping("/description")
	@Operation(summary = "프로 설명 고치기")
	public ApiResponse<String> updateProDescription(
		@Valid @RequestBody DescriptionUpdateRequestDTO request) {
		Long userId = SecurityUtils.currentUserIdOrThrow();
		profileFacade.updateProDescription(userId, request);
		return ApiResponse.onSuccess("전문가 소개가 수정되었습니다.");
	}

	@PatchMapping(value = "/photos", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	@Operation(summary = "프로 사진 변경", description = "{\n"
		+ "  \"existingPhotoUrls\": [\n"
		+ "    \"test\"\n"
		+ "  ]\n"
		+ "}")
	public ApiResponse<String> updateProPhotos(
		@RequestPart(value = "request") String requestJson, // JSON 데이터를 DTO로 받음
		@RequestPart(value = "newPhotos", required = false) List<MultipartFile> newPhotoFiles) throws Exception {

		Long userId = SecurityUtils.currentUserIdOrThrow();
		PhotoUpdateRequestDTO updateRequest = objectMapper.readValue(requestJson, PhotoUpdateRequestDTO.class);
		profileFacade.updateProPhotos(userId, updateRequest, newPhotoFiles);

		return ApiResponse.onSuccess("사진이 성공적으로 수정되었습니다.");
	}

	@PatchMapping("/ptPrice")
	@Operation(summary = "pt 가격 수정")
	public ApiResponse<String> updateProPtPrice(
		@Valid @RequestBody PtPriceRequest.PtPriceUpdateRequestList request) {
		Long userId = SecurityUtils.currentUserIdOrThrow();
		profileFacade.updateProPtPrice(userId, request);
		return ApiResponse.onSuccess("PT 가격 정보가 수정되었습니다.");
	}

	@PatchMapping("/ptProgram")
	@Operation(summary = "pt 프로그램 수정")
	public ApiResponse<String> updateProProgram(
		@Valid @RequestBody PtProgramUpdateRequestDTO request) {
		Long userId = SecurityUtils.currentUserIdOrThrow();
		profileFacade.updateProProgram(userId, request);
		return ApiResponse.onSuccess("PT 프로그램 정보가 수정되었습니다.");
	}

	@PatchMapping("/location")
	@Operation(summary = "전문가 위치 정보 수정 API", description = "전문가의 센터 및 대표 주소 정보를 수정합니다.")
	public ApiResponse<String> updateProLocation(
		@RequestBody @Valid ProLocationUpdateRequestDTO request) {

		Long userId = SecurityUtils.currentUserIdOrThrow();
		profileFacade.updateProLocation(userId, request);
		return ApiResponse.onSuccess("위치 정보가 성공적으로 수정되었습니다.");
	}
}

