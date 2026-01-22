package com.grabpt.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.domain.entity.Requestions;
import com.grabpt.dto.request.RequestionRequestDto;
import com.grabpt.dto.response.RequestionResponseDto;
import com.grabpt.dto.response.UserResponseDto;
import com.grabpt.service.RequestionService.RequestionService;
import com.grabpt.service.UserService.UserQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/requestion")
@RequiredArgsConstructor
@Slf4j
public class RequestionController {

	private final RequestionService requestionService;
	private final UserQueryService userQueryService;

	@Operation(
		summary = "요청서 작성 저장 API",
		description = "작성한 요청서를 저장"
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200", description = "저장 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = RequestionResponseDto.RequestionSaveResponseDto.class),
				examples = @ExampleObject(name = "성공 예시", value = """
					{
					  "isSuccess": true,
					  "code": "OK",
					  "message": "요청에 성공했습니다.",
					  "result": { "requestRequestionId": 101 }
					}
					""")
			)
		)
	})
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
		required = true,
		description = "RequestionRequestDto JSON 본문",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = RequestionRequestDto.class),
			examples = @ExampleObject(name = "요청 예시", value = """
				{
				  "categoryId": 1,
				  "price": 50000,
				  "sessionCount": 1,
				  "purpose": ["다이어트", "체력향상"],
				  "etcPurposeContent": "하체 강화",
				  "content": "주 2회 진행 원합니다.",
				  "ageGroup": "20",
				  "userGender": "남자",
				  "availableDays": ["월", "수", "금"],
				  "availableTimes": ["오전", "저녁"],
				  "proGender": "남자",
				  "startDate": "2025-10-01",
				  "location": "서울 강남구 성북동"
				}
				""")
		)
	)
	@PostMapping
	public ApiResponse<RequestionResponseDto.RequestionSaveResponseDto> setRequestion(
		@RequestBody RequestionRequestDto dto,
		HttpServletRequest request) throws IllegalAccessException {

		UserResponseDto.UserInfoDTO userInfo = userQueryService.getUserInfo(request);
		String email = userInfo.getEmail();
		Requestions saved = requestionService.save(dto, email);

		return ApiResponse.onSuccess(
			RequestionResponseDto.RequestionSaveResponseDto.builder()
				.requestRequestionId(saved.getId())
				.build()
		);
	}

	@Operation(
		summary = "요청서 상세 조회 API",
		description = "요청서와 유저 정보를 함께 조회합니다."
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200", description = "조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = RequestionResponseDto.RequestionDetailResponseDto.class),
				examples = @ExampleObject(name = "성공 예시", value = """
					{
					  "isSuccess": true,
					  "code": "OK",
					  "message": "요청에 성공했습니다.",
					  "result": {
					    "requestionId": 101,
					    "categoryId": 1,
					    "purposes": ["다이어트", "체력향상"],
					    "ageGroup": "20",
					    "userGender": "남자",
					    "requestedPrice": 50000,
					    "sessionCount": 1,
					    "location": "서울 강남구 성북동",
					    "startDate": "2025-10-01",
					    "availableDays": ["월","수","금"],
					    "availableTimes": ["오전","저녁"],
					    "proGender": "남자",
					    "content": "주 2회 진행 원합니다.",
					    "etcPurposeContent": "하체 강화",
					    "userNickname": "홍길동",
					    "profileImageUrl": "https://cdn.example.com/u/1.jpg"
					  }
					}
					""")
			)
		)
	})
	@GetMapping("/{requestionId}")
	public ApiResponse<RequestionResponseDto.RequestionDetailResponseDto> getRequestionDetail(
		@Parameter(description = "요청서 ID", schema = @Schema(type = "integer", example = "101"))
		@PathVariable Long requestionId
	) {
		RequestionResponseDto.RequestionDetailResponseDto response = requestionService.getDetail(requestionId);
		return ApiResponse.onSuccess(response);
	}

	@Operation(
		summary = "요청서 조회 api",
		description = "트레이너 기준 일반 유저의 요청서를 조회합니다."
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200", description = "조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = Page.class),
				examples = @ExampleObject(name = "성공 예시", value = """
					{
					  "isSuccess": true,
					  "code": "OK",
					  "message": "요청에 성공했습니다.",
					  "result": {
					    "content": [
					      {
					        "userName": "김민수",
					        "location": "강남구 역삼동",
					        "sessionCount": 8,
					        "requestedPrice": 60000,
					        "matchingStatus": "PENDING",
					        "profileImageUrl": "https://cdn.example.com/u/10.jpg",
					        "requestionId": 201,
					        "content": "체형 교정 위주로 부탁드려요.",
					        "etcPurposeContent": "거북목 완화",
					        "availableDays": ["화","목"],
					        "availableTimes": ["저녁"],
					        "categoryName": "체형 교정",
					        "userNickname": "Minsu"
					      },
					      {
					        "userName": "이수지",
					        "location": "성동구 성수동",
					        "sessionCount": 12,
					        "requestedPrice": 55000,
					        "matchingStatus": "PENDING",
					        "profileImageUrl": "https://cdn.example.com/u/11.jpg",
					        "requestionId": 202,
					        "content": "다이어트 PT 원해요.",
					        "etcPurposeContent": null,
					        "availableDays": ["월","수","금"],
					        "availableTimes": ["오전","점심"],
					        "categoryName": "다이어트",
					        "userNickname": "Suzy"
					      }
					    ],
					    "pageable": { "pageNumber": 0, "pageSize": 4 },
					    "totalElements": 23,
					    "totalPages": 6,
					    "last": false,
					    "size": 4,
					    "number": 0
					  }
					}
					""")
			)
		)
	})
	@GetMapping("/nearby")
	public ApiResponse<Page<RequestionResponseDto.RequestionResponsePagingDto>> getRequestionsNearby(
		@Parameter(description = "정렬 기준", schema = @Schema(type = "string", example = "latest"))
		@RequestParam(defaultValue = "latest") String sortBy,
		@Parameter(description = "페이지(1부터 시작)", schema = @Schema(type = "integer", example = "1"))
		@RequestParam(defaultValue = "1") int page,
		@Parameter(description = "페이지 크기", schema = @Schema(type = "integer", example = "4"))
		@RequestParam(defaultValue = "4") int size,
		HttpServletRequest request
	) throws IllegalAccessException {
		Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
		Page<RequestionResponseDto.RequestionResponsePagingDto> response =
			requestionService.getNearbyRequestions(request, sortBy, pageable);
		return ApiResponse.onSuccess(response);
	}

	@Operation(
		summary = "요청서 수정 API",
		description = "사용자가 본인의 요청서를 수정합니다."
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200", description = "수정 성공",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(name = "성공 예시", value = """
					{
					  "isSuccess": true,
					  "code": "OK",
					  "message": "요청에 성공했습니다.",
					  "result": "요청서가 성공적으로 수정되었습니다."
					}
					""")
			)
		)
	})
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
		required = true,
		description = "RequestionRequestDto JSON 본문 (부분 업데이트 시 Service에서 null 필드 무시 처리 권장)",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = RequestionRequestDto.class),
			examples = @ExampleObject(name = "요청 예시", value = """
				{
				  "price": 52000,
				  "sessionCount": 8,
				  "availableDays": ["화","목"],
				  "availableTimes": ["저녁"],
				  "content": "저녁 시간 위주로 가능해요."
				}
				""")
		)
	)
	@PatchMapping("/{requestionId}")
	public ApiResponse<String> updateRequestion(
		@Parameter(description = "요청서 ID", schema = @Schema(type = "integer", example = "101"))
		@PathVariable Long requestionId,
		@RequestBody RequestionRequestDto dto,
		HttpServletRequest request
	) throws IllegalAccessException {
		UserResponseDto.UserInfoDTO userInfo = userQueryService.getUserInfo(request);
		String email = userInfo.getEmail();

		requestionService.update(requestionId, dto, email);
		return ApiResponse.onSuccess("요청서가 성공적으로 수정되었습니다.");
	}

	@Operation(
		summary = "요청서 삭제 API",
		description = "사용자가 본인의 요청서를 삭제합니다."
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200", description = "삭제 성공",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(name = "성공 예시", value = """
					{
					  "isSuccess": true,
					  "code": "OK",
					  "message": "요청에 성공했습니다.",
					  "result": "요청서가 본인의 요청서를 삭제되었습니다."
					}
					""")
			)
		)
	})
	@DeleteMapping("/{requestionId}")
	public ApiResponse<String> deleteRequestion(
		@Parameter(description = "요청서 ID", schema = @Schema(type = "integer", example = "101"))
		@PathVariable Long requestionId,
		HttpServletRequest request
	) throws IllegalAccessException {
		UserResponseDto.UserInfoDTO userInfo = userQueryService.getUserInfo(request);
		String email = userInfo.getEmail();

		requestionService.delete(requestionId, email);
		return ApiResponse.onSuccess("요청서가 성공적으로 삭제되었습니다.");
	}

	@Operation(
		summary = "내 요청서 목록 조회 API",
		description = "회원이 본인이 작성한 요청서 목록을 조회합니다."
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200", description = "조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = Page.class),
				examples = @ExampleObject(name = "성공 예시", value = """
					{
					  "isSuccess": true,
					  "code": "OK",
					  "message": "요청에 성공했습니다.",
					  "result": {
					    "content": [
					      {
					        "requestId": 1001,
					        "profileImageURL": "https://cdn.example.com/u/1.jpg",
					        "availableDays": ["월","수","금"],
					        "availableTimes": ["오전","저녁"],
					        "categoryName": "다이어트",
					        "sessionCount": 8,
					        "content": "주 2회 진행 원합니다.",
					        "address": {
					          "city": "서울",
					          "district": "강남구",
					          "street": "역삼동",
					          "zipcode": "06236",
					          "streetCode": "11680",
					          "specAddress": "테헤란로 123"
					        },
					        "isWriteReview": false
					      }
					    ],
					    "pageable": { "pageNumber": 0, "pageSize": 3 },
					    "totalElements": 3,
					    "totalPages": 1,
					    "last": true,
					    "size": 3,
					    "number": 0
					  }
					}
					""")
			)
		)
	})
	@GetMapping("/my")
	public ApiResponse<Page<RequestionResponseDto.UserOwnRequestionDto>> getMyRequestions(
		@Parameter(description = "페이지(1부터 시작)", schema = @Schema(type = "integer", example = "1"))
		@RequestParam(defaultValue = "1") int page,
		@Parameter(description = "페이지 크기", schema = @Schema(type = "integer", example = "3"))
		@RequestParam(defaultValue = "3") int size,
		HttpServletRequest request
	) throws IllegalAccessException {
		Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
		Page<RequestionResponseDto.UserOwnRequestionDto> response = requestionService.getRequestionsByUser(request,
			pageable);
		return ApiResponse.onSuccess(response);
	}

	@Operation(
		summary = "요청서 수정 가능 여부 확인",
		description = "현재 로그인한 사용자가 요청서를 작성했는지 확인합니다."
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200", description = "조회 성공",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(name = "성공 예시", value = """
					{
					  "isSuccess": true,
					  "code": "OK",
					  "message": "요청에 성공했습니다.",
					  "result": { "isEdit": true }
					}
					""")
			)
		)
	})
	@GetMapping("/{requestionId}/requestion-can-edit")
	public ApiResponse<Map<String, Boolean>> canEditRequestion(
		@Parameter(description = "요청서 ID", schema = @Schema(type = "integer", example = "101"))
		@PathVariable Long requestionId,
		HttpServletRequest request
	) throws IllegalAccessException {
		UserResponseDto.UserInfoDTO userInfo = userQueryService.getUserInfo(request);
		String email = userInfo.getEmail();

		boolean isEdit = requestionService.canEditRequestion(requestionId, email);

		Map<String, Boolean> response = new HashMap<>();
		response.put("isEdit", isEdit);
		return ApiResponse.onSuccess(response);
	}

}
