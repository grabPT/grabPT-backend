package com.grabpt.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.UserHandler;
import com.grabpt.domain.entity.Users;
import com.grabpt.dto.request.ReviewRequestDTO;
import com.grabpt.dto.response.MyReviewListDTO;
import com.grabpt.dto.response.UserResponseDto;
import com.grabpt.repository.UserRepository.UserRepository;
import com.grabpt.service.ProfileService.ProfileService;
import com.grabpt.service.ReviewService.ReviewService;
import com.grabpt.service.UserService.UserQueryService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
	private final ReviewService reviewService;
	private final UserQueryService userQueryService;
	private final ProfileService profileService;


	@Operation(
		description = "해당 전문가의 리뷰 확인",
		summary = "해당 전문가의 리뷰 확인"
	)
	@GetMapping("/{userId}")
	public ApiResponse<Page<MyReviewListDTO>> getProReviews(
		@PathVariable(name = "userId") Long userId,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size) {

		Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
		Page<MyReviewListDTO> reviews = profileService.findProReviews(userId, pageable);
		return ApiResponse.onSuccess(reviews);
	}

	@Operation(summary = "리뷰 작성 API", description = "사용자가 전문가에 대한 리뷰를 작성합니다.")
	@PostMapping
	public ApiResponse<String> addReview(HttpServletRequest request
		, @RequestBody ReviewRequestDTO reviewRequestDTO) throws IllegalAccessException {
		UserResponseDto.UserInfoDTO userInfo = userQueryService.getUserInfo(request);
		Long userId = userInfo.getUserId();
		reviewService.createReview(userId, reviewRequestDTO);
		return ApiResponse.onSuccess("리뷰가 성공적으로 등록되었습니다.");
	}

	@Operation(summary = "리뷰 삭제 API")
	@DeleteMapping
	public ApiResponse<String> deleteReview(HttpServletRequest request,@RequestParam Long reviewId) throws IllegalAccessException {
		UserResponseDto.UserInfoDTO userInfo = userQueryService.getUserInfo(request);
		Long userId = userInfo.getUserId();

		reviewService.deleteReview(userId,reviewId);
		return ApiResponse.onSuccess("리뷰가 성공적으로 삭제되었습니다.");
	}
}
