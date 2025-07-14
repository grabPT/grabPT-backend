package com.grabpt.controller;

import java.util.List;

import com.grabpt.service.RequestionService.RequestionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.converter.CategoryConverter;
import com.grabpt.domain.entity.Category;
import com.grabpt.domain.entity.Requestions;
import com.grabpt.domain.enums.RequestStatus;
import com.grabpt.dto.response.CategoryResponse;
import com.grabpt.service.CategoryService.CategoryQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CategoryRestController {

	private final CategoryQueryService categoryQueryService;
	private final RequestionService requestionService;

	@Operation(
		summary = "카테고리 목록 조회 API",
		description = "카테고리의 목록을 조회하는 API"
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
	})
	@GetMapping("/categories")
	public ApiResponse<List<CategoryResponse.CategoryListDto>> getCategoryList() {
		List<Category> categories = categoryQueryService.getCategories();
		return ApiResponse.onSuccess(CategoryConverter.toCategoryListDto(categories));
	}

	@Operation(
		summary = "카테고리 상세 페이지 조회(전문가 목록 조회)",
		description = "카테고리 상세 페이지 중 전문가 목록을 조회하는 API(페이징 포함), 로그아웃 상태일 때 region을 쿼리파라미터로 전달"
	)
	@Parameters({
		@Parameter(name = "code", description = "카테고리 코드 (예: box)", required = true, in = ParameterIn.PATH, example = "box"),
		@Parameter(name = "region", description = "지역명 (예: 화곡3동)", required = false, example = "화곡3동"),
		@Parameter(name = "page", description = "페이지 번호 1부터 시작)", required = false, example = "1")
	})
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "전문가 목록 조회 성공",
			content = @Content(mediaType = "application/json",
				schema = @Schema(implementation = CategoryResponse.TrainerPreviewListDto.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
	})
	@GetMapping("/category/{code}/trainers")
	public ApiResponse<CategoryResponse.TrainerPreviewListDto> getTrainerList(
		@PathVariable(name = "code") String code,
		@RequestParam(name = "region", required = false) String region,
		@RequestParam(defaultValue = "1") int page) {

		Pageable pageable = PageRequest.of(page - 1, 4, Sort.by("rating").descending());
		return ApiResponse.onSuccess(new CategoryResponse.TrainerPreviewListDto(null, 0, 0, 0L, true, true));
	}

	@Operation(
		summary = "요청서 목록 조회 (카테고리별)",
		description = "카테고리 코드에 해당하는 요청서 목록을 최신순으로 6개를 보여줍니다, 지역은 전국 단위입니다"
	)
	@Parameters({
		@Parameter(name = "code", description = "카테고리 코드 (예: box)", required = true, in = ParameterIn.PATH, example = "box"),
	})
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "요청서 목록 조회 성공",
			content = @Content(mediaType = "application/json",
				schema = @Schema(implementation = CategoryResponse.RequestListDto.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
	})
	@GetMapping("/requests/{code}")
	public ApiResponse<List<CategoryResponse.RequestListDto>> getRequestList(@PathVariable(name = "code") String code) {

		Pageable pageable = PageRequest.of(0,6);
		List<Requestions> reqeustions = requestionService.getReqeustions(code, pageable);
		return ApiResponse.onSuccess(CategoryConverter.toRequestListDto(reqeustions));
	}

}
