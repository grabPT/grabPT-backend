package com.grabpt.controller;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.dto.request.ChatRequest;
import com.grabpt.service.ChatService.ChatFileService;
import com.grabpt.service.PhotoService.PhotoService;
import com.grabpt.service.UserService.UserQueryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ChatFileController {

	private final UserQueryService userQueryService;
	private final ChatFileService chatFileService;

	@PostMapping("/chatRoom/{roomId}/upload")
	public ApiResponse<ChatRequest.MessageRequestDto> uploadChatFile(@PathVariable(name = "roomId") Long roomId, @RequestPart("file") MultipartFile file,
																	 HttpServletRequest request) throws IllegalAccessException {
		Long userId = userQueryService.getUserId(request);
		return ApiResponse.onSuccess(chatFileService.uploadChatFile(roomId, userId, file));
	}
}
