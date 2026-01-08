package com.grabpt.controller;

import java.util.List;

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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

	@Operation(
		summary = "제안서 저장 API (Multipart)",
		description = "트레이너가 보낸 제안서를 저장합니다. `data`(JSON)와 `photos`(이미지 리스트)를 함께 업로드하세요."
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200", description = "저장 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = SuggestionResponseDto.SuggestionSaveResponseDto.class),
				examples = @ExampleObject(name = "성공 예시", value = """
					{
					  "isSuccess": true,
					  "code": "OK",
					  "message": "요청에 성공했습니다.",
					  "result": { "suggestionId": 123 }
					}
					""")
			)
		)
	})
	// 핵심: Swagger UI가 multipart 각 필드를 인식하도록 메서드 레벨에서 정의
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
		content = @Content(
			mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
			schema = @Schema(type = "object"),
			encoding = @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE),
			examples = @ExampleObject(
				name = "data 예시",
				summary = "제안서 본문 예시",
				value = """
					{
					  "requestionId": 77,
					  "price": 50000,
					  "sessionCount": 10,
					  "message": "안녕하세요, 반갑습니다.",
					  "location": "성북동",
					  "sentAt": "2025-10-01",
					  "isMatched": false
					}
					"""
			)
		)
	)
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse<SuggestionResponseDto.SuggestionSaveResponseDto> setSuggestion(
		// Swagger UI에서 이 파트를 JSON 입력창으로 인식하게 함
		@RequestPart("data") SuggestionRequestDto dto,

		// Swagger UI에서 이 파트를 '파일 선택' 버튼으로 인식하게 함 (format = binary 필수)
		@RequestPart(value = "photos", required = false)
		@Parameter(
			description = "첨부 이미지 배열",
			content = @Content(array = @ArraySchema(schema = @Schema(type = "string", format = "binary")))
		)
		List<MultipartFile> photos,

		HttpServletRequest request
	) throws IllegalAccessException {

		UserResponseDto.UserInfoDTO userInfo = userQueryService.getUserInfo(request);
		String email = userInfo.getEmail();

		Suggestions saved = suggestionService.save(dto, email, photos);

		return ApiResponse.onSuccess(
			SuggestionResponseDto.SuggestionSaveResponseDto.builder()
				.suggestionId(saved.getId())
				.build()
		);
	}

	@Operation(
		summary = "제안서 상세 조회 API",
		description = "제안서 정보와 트레이너 프로필 정보를 조회합니다."
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200", description = "조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = SuggestionResponseDto.SuggestionDetailResponseDto.class),
				examples = @ExampleObject(name = "성공 예시", value = """
					{
					  "isSuccess": true,
					  "code": "OK",
					  "message": "요청에 성공했습니다.",
					  "result": {
					    "userNickName": "홍길동",
					    "centerName": "그랩PT 강남점",
					    "profileImageUrl": "https://cdn.example.com/u/1.jpg",

					    "suggestedPrice": 50000,
					    "requestedPrice": 60000,
					    "discountAmount": 10000,
					    "isDiscounted": true,

					    "message": "안녕하세요, 반갑습니다.",
					    "location": "성북동",
					    "photos": [
					      "https://cdn.example.com/s/123/1.jpg",
					      "https://cdn.example.com/s/123/2.jpg"
					    ],

					    "proId": 11,
					    "userId": 99,
					    "matchingId": 101,
					    "requestionId": 77,
					    "suggestionId": 123
					  }
					}
					""")
			)
		)
	})
	@GetMapping("/{suggestionId}")
	public ApiResponse<SuggestionResponseDto.SuggestionDetailResponseDto> getSuggestionDetail(
		@Parameter(description = "제안서 ID", schema = @Schema(type = "integer", example = "123"))
		@PathVariable Long suggestionId) {
		SuggestionResponseDto.SuggestionDetailResponseDto response = suggestionService.getDetail(suggestionId);
		return ApiResponse.onSuccess(response);
	}

	@Operation(
		summary = "요청서에 대한 제안서 목록 조회 API",
		description = "요청서 ID에 해당하는 제안서를 6개씩 페이징하여 조회합니다. page는 1부터 시작."
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
					        "userNickname": "홍길동",
					        "centerName": "그랩PT 강남점",
					        "location": "성북동",
					        "suggestedPrice": 50000,
					        "averageRating": 4.9,
					        "sessionCount": 10,
					        "profileImageUrl": "https://cdn.example.com/u/1.jpg",
					        "suggestionId": 201
					      },
					      {
					        "userNickname": "김철수",
					        "centerName": "그랩PT 성수점",
					        "location": "성수동",
					        "suggestedPrice": 48000,
					        "averageRating": 4.7,
					        "sessionCount": 8,
					        "profileImageUrl": "https://cdn.example.com/u/2.jpg",
					        "suggestionId": 202
					      }
					    ],
					    "pageable": { "pageNumber": 0, "pageSize": 6 },
					    "totalElements": 42,
					    "totalPages": 7,
					    "last": false,
					    "size": 6,
					    "number": 0
					  }
					}
					""")
			)
		)
	})
	@GetMapping("/suggestion/suggestionList/{requestionId}")
	public ApiResponse<Page<SuggestionResponseDto.SuggestionResponsePagingDto>> getSuggestionsByRequestion(
		@Parameter(description = "요청서 ID", schema = @Schema(type = "integer", example = "77"))
		@PathVariable Long requestionId,
		@Parameter(description = "페이지(1부터 시작)", schema = @Schema(type = "integer", example = "1"))
		@RequestParam(defaultValue = "1") int page
	) {
		int adjustedPage = Math.max(page - 1, 0);
		Page<SuggestionResponseDto.SuggestionResponsePagingDto> result =
			suggestionService.getSuggestionsByRequestionId(requestionId, adjustedPage);
		return ApiResponse.onSuccess(result);
	}

	@Operation(
		summary = "트레이너 제안서 목록 조회 API",
		description = "로그인한 트레이너가 작성한 제안서를 8개씩 페이징하여 조회합니다. page는 1부터 시작."
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200", description = "조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = Page.class),
				examples = @ExampleObject(
					name = "성공 예시",
					value = """
						{
						  "isSuccess": true,
						  "code": "OK",
						  "message": "요청에 성공했습니다.",
						  "result": {
						    "content": [
						      {
						        "userNickname": "홍길동",
						        "suggestedPrice": 50000,
						        "sessionCount": 10,
						        "matchingStatus": "MATCHED",
						        "profileImageUrl": "https://cdn.example.com/u/1.jpg",
						        "requestionId": 77,
						        "suggestionId": 301
						      },
						      {
						        "userNickname": "김철수",
						        "suggestedPrice": 48000,
						        "sessionCount": 8,
						        "matchingStatus": "PENDING",
						        "profileImageUrl": "https://cdn.example.com/u/2.jpg",
						        "requestionId": 82,
						        "suggestionId": 302
						      }
						    ],
						    "pageable": { "pageNumber": 0, "pageSize": 8 },
						    "totalElements": 12,
						    "totalPages": 2,
						    "last": false,
						    "size": 8,
						    "number": 0
						  }
						}
						"""
				)
			)
		)
	})
	@GetMapping("/mySuggestions")
	public ApiResponse<Page<SuggestionResponseDto.MySuggestionPagingDto>> getMySuggestions(
		HttpServletRequest request,
		@Parameter(description = "페이지(1부터 시작)", schema = @Schema(type = "integer", example = "1"))
		@RequestParam(defaultValue = "1") int page
	) throws IllegalAccessException {
		Page<SuggestionResponseDto.MySuggestionPagingDto> response = suggestionService.getMySuggestions(request, page);
		return ApiResponse.onSuccess(response);
	}

	@Operation(
		summary = "제안서 수정 API",
		description = "작성자가 본인의 제안서를 수정합니다."
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200", description = "수정 성공",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					name = "성공 예시",
					value = """
						{
						  "isSuccess": true,
						  "code": "OK",
						  "message": "요청에 성공했습니다.",
						  "result": "제안서가 성공적으로 수정되었습니다."
						}
						"""
				)
			)
		)
	})
	@PatchMapping("/{suggestionId}")
	public ApiResponse<String> updateSuggestion(
		@Parameter(description = "제안서 ID", schema = @Schema(type = "integer", example = "123"))
		@PathVariable Long suggestionId,
		@io.swagger.v3.oas.annotations.parameters.RequestBody(
			required = true,
			description = "수정할 제안서 JSON 본문",
			content = @Content(
				schema = @Schema(implementation = SuggestionRequestDto.class),
				examples = @ExampleObject(name = "요청 예시", value = """
					{
					  "title": "업데이트된 프로그램 제목",
					  "content": "설명 보강",
					  "price": 500000
					}
					""")
			)
		)
		@RequestBody SuggestionRequestDto dto,
		HttpServletRequest request
	) throws IllegalAccessException {

		UserResponseDto.UserInfoDTO userInfo = userQueryService.getUserInfo(request);
		String email = userInfo.getEmail();

		suggestionService.updateSuggestion(suggestionId, dto, email);
		return ApiResponse.onSuccess("제안서가 성공적으로 수정되었습니다.");
	}

	@Operation(
		summary = "제안서 삭제 API",
		description = "작성자가 본인의 제안서를 삭제합니다."
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200", description = "삭제 성공",
			content = @Content(mediaType = "application/json",
				examples = @ExampleObject(name = "성공 예시", value = """
					{
					  "isSuccess": true,
					  "code": "OK",
					  "message": "요청에 성공했습니다.",
					  "result": "제안서가 성공적으로 삭제되었습니다."
					}
					""")
			)
		)
	})
	@DeleteMapping("/{suggestionId}")
	public ApiResponse<String> deleteSuggestion(
		@Parameter(description = "제안서 ID", schema = @Schema(type = "integer", example = "123"))
		@PathVariable Long suggestionId,
		HttpServletRequest request
	) throws IllegalAccessException {

		UserResponseDto.UserInfoDTO userInfo = userQueryService.getUserInfo(request);
		String email = userInfo.getEmail();

		suggestionService.deleteSuggestion(suggestionId, email);
		return ApiResponse.onSuccess("제안서가 성공적으로 삭제되었습니다.");
	}

}
