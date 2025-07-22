package com.grabpt.service.ChatService;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.grabpt.aws.s3.AmazonS3Manager;
import com.grabpt.aws.s3.Uuid;
import com.grabpt.config.AmazonConfig;
import com.grabpt.dto.request.ChatRequest;
import com.grabpt.dto.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatFileServiceImpl implements ChatFileService{

	private final AmazonS3Manager amazonS3Manager;
	private final AmazonConfig amazonConfig;
	private final AmazonS3Client s3Client;

	@Override
	public ChatRequest.MessageRequestDto uploadChatFile(Long roomId, Long userId, MultipartFile file) {
		if(file == null || file.isEmpty()){
			return null;
		}
		log.info("bucket name: {}", amazonConfig.getBucket());

		Uuid uuid = Uuid.builder().uuid(java.util.UUID.randomUUID().toString()).build();
		String keyName = amazonS3Manager.generateReviewKeyName(uuid);
		String fileUrl = amazonS3Manager.uploadFile(keyName, file);
		String messageType = file.getContentType() != null && file.getContentType()
				.startsWith("image/") ? "IMAGE" : "FILE";

		log.info("message Type: {}",messageType);
		return ChatRequest.MessageRequestDto.builder()
			.content(fileUrl)
			.messageType(messageType)
			.senderId(userId)
			.roomId(roomId)
			.build();
	}


}
