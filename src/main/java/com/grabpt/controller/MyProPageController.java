package com.grabpt.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
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
import com.grabpt.dto.request.CenterUpdateRequestDTO;
import com.grabpt.dto.request.CertificationUpdateRequestDTO;
import com.grabpt.dto.request.DescriptionUpdateRequestDTO;
import com.grabpt.dto.request.ProLocationUpdateRequestDTO;
import com.grabpt.dto.request.PtPriceUpdateRequestDTO;
import com.grabpt.dto.request.PtProgramUpdateRequestDTO;
import com.grabpt.dto.response.CertificationResponseDTO;
import com.grabpt.dto.response.MyReviewListDTO;
import com.grabpt.dto.response.ProfileResponseDTO;
import com.grabpt.service.ProfileService.ProfileService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/mypage/pro")
@RequiredArgsConstructor
public class MyProPageController {

	private final ProfileService profileService;
	private final ObjectMapper objectMapper;

	@GetMapping
	public ApiResponse<ProfileResponseDTO.MyProProfileDTO> getMyProUserProfile(
		@RequestParam(name = "userId") Long userId) {
		return ApiResponse.onSuccess(profileService.findMyProUserProfile(userId));
	}

	@GetMapping("/reviews")
	public ApiResponse<Page<MyReviewListDTO>> getProReviews(
		@RequestParam(name = "userId") Long userId,
		Pageable pageable) {

		Page<MyReviewListDTO> reviews = profileService.findProReviews(userId, pageable);
		return ApiResponse.onSuccess(reviews);
	}

	@GetMapping("/certification")
	@Operation(summary = "전문가 자격증/이력 조회 API")
	public ApiResponse<CertificationResponseDTO> getProCertifications(
		@RequestParam(name = "userId") Long userId) {
		CertificationResponseDTO certifications = profileService.findMyCertifications(userId);
		return ApiResponse.onSuccess(certifications);
	}

	@PostMapping(value = "/certification", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	@Operation(summary = "전문가 자격증/이력 등록 API", description = "새로운 이력 정보를 추가합니다.")
	public ApiResponse<String> registerProCertifications(
		@RequestParam(name = "userId") Long userId,
		@RequestParam("request") String requestJson, // DTO를 String으로 받음
		@RequestPart(value = "images", required = false) List<MultipartFile> images) throws Exception {

		// JSON 문자열을 DTO 객체로 변환
		CertificationUpdateRequestDTO request = objectMapper.readValue(requestJson, CertificationUpdateRequestDTO.class);

		profileService.updateProCertifications(userId, request, images);
		return ApiResponse.onSuccess("자격증 정보가 성공적으로 등록되었습니다.");
	}

	@PatchMapping("/center")
	public ApiResponse<String> updateProCenter(
		@RequestParam(name = "userId") Long userId,
		@Valid @RequestBody CenterUpdateRequestDTO request) {

		profileService.updateProCenter(userId, request);
		return ApiResponse.onSuccess("센터 정보 수정이 완료되었습니다.");
	}

	@PatchMapping("/description")
	public ApiResponse<String> updateProDescription(
		@RequestParam(name = "userId") Long userId,
		@Valid @RequestBody DescriptionUpdateRequestDTO request) {
		profileService.updateProDescription(userId, request);
		return ApiResponse.onSuccess("전문가 소개가 수정되었습니다.");
	}

	@PatchMapping(value = "/photos", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	public ApiResponse<String> updateProPhotos(
		@RequestParam(name = "userId") Long userId,
		@RequestPart(value = "photos") List<MultipartFile> photoFiles) {

		// profileService의 public 메서드를 호출합니다.
		profileService.updateProPhotos(userId, photoFiles);

		return ApiResponse.onSuccess("사진이 성공적으로 수정되었습니다.");
	}

	@PatchMapping("/ptPrice")
	public ApiResponse<String> updateProPtPrice(
		@RequestParam(name = "userId") Long userId,
		@Valid @RequestBody PtPriceUpdateRequestDTO request) {
		profileService.updateProPtPrice(userId, request);
		return ApiResponse.onSuccess("PT 가격 정보가 수정되었습니다.");
	}

	@PatchMapping("/ptProgram")
	public ApiResponse<String> updateProProgram(
		@RequestParam(name = "userId") Long userId,
		@Valid @RequestBody PtProgramUpdateRequestDTO request) {
		profileService.updateProProgram(userId, request);
		return ApiResponse.onSuccess("PT 프로그램 정보가 수정되었습니다.");
	}

	@PatchMapping("/location")
	@Operation(summary = "전문가 위치 정보 수정 API", description = "전문가의 센터 및 대표 주소 정보를 수정합니다.")
	public ApiResponse<String> updateProLocation(
		@RequestParam(name = "userId") Long userId,
		@RequestBody @Valid ProLocationUpdateRequestDTO request) {

		profileService.updateProLocation(userId, request);
		return ApiResponse.onSuccess("위치 정보가 성공적으로 수정되었습니다.");
	}
}

