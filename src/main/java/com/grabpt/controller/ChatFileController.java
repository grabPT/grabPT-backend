package com.grabpt.controller;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.config.SecurityUtils;
import com.grabpt.dto.request.ChatRequest;
import com.grabpt.dto.response.CategoryResponse;
import com.grabpt.service.ChatService.ChatFileService;
import com.grabpt.service.PhotoService.PhotoService;
import com.grabpt.service.UserService.UserQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ChatFileController {

	private final UserQueryService userQueryService;
	private final ChatFileService chatFileService;

	@Operation(
		summary = "채팅방 파일 및 이미지 업로드 API",
		description = "roomId를 통해 채팅방의 파일 및 이미지를 업로드 합니다. 인증 토큰을 필요로 합니다"
	)
	@Parameters({
		@Parameter(name = "roomId", description = "채팅방 Id", required = true, in = ParameterIn.PATH, example = "2"),
	})
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "파일 및 이미지 업로드 성공",
			content = @Content(mediaType = "application/json",
				schema = @Schema(implementation = ChatRequest.MessageRequestDto.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
	})
	@PostMapping("/chatRoom/{roomId}/upload")
	public ApiResponse<ChatRequest.MessageRequestDto> uploadChatFile(@PathVariable(name = "roomId") Long roomId,
																	 @Parameter(
																		 description = "업로드할 파일 (jpg, png, pdf 등)",
																		 required = true,
																		 content = @Content(
																			 mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
																			 schema = @Schema(type = "string", format = "binary", example = "test.png")
																		 )
																	 )
																	 @RequestPart("file") MultipartFile file){
		Long userId = SecurityUtils.currentUserIdOrThrow();
		return ApiResponse.onSuccess(chatFileService.uploadChatFile(roomId, userId, file));
	}
}
