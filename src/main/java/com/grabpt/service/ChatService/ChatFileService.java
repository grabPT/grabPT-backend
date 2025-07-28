package com.grabpt.service.ChatService;

import com.grabpt.dto.request.ChatRequest;
import com.grabpt.dto.response.ChatResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ChatFileService {
	ChatRequest.MessageRequestDto uploadChatFile(Long roomId, Long userId, MultipartFile file);
}
