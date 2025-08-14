package com.grabpt.aws.s3;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.grabpt.config.AmazonConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AmazonS3Manager {

	private final AmazonS3 amazonS3;

	private final AmazonConfig amazonConfig;

	private static final ZoneId ZONE_SEOUL = ZoneId.of("Asia/Seoul");

	public String uploadFile(String keyName, MultipartFile file) {
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(file.getSize());
		metadata.setContentType(file.getContentType());
		try {
			amazonS3.putObject(
				new PutObjectRequest(amazonConfig.getBucket(), keyName, file.getInputStream(), metadata));
		} catch (IOException e) {
			log.error("error at AmazonS3Manager uploadFile : {}", (Object)e.getStackTrace());
		}

		return amazonS3.getUrl(amazonConfig.getBucket(), keyName).toString();
	}

	public String generateProPhotoKeyName(Uuid uuid) {
		return amazonConfig.getProPhoto() + '/' + uuid.getUuid();
	}

	public String generateUserPhotoKeyName(Uuid uuid) {
		return amazonConfig.getUserPhoto() + '/' + uuid.getUuid();
	}

	public String generateTestKeyName(Uuid uuid) {
		return amazonConfig.getUserPhoto() + '/' + uuid.getUuid();
	}

	public String generateProfilePhotoKeyName(Uuid uuid) {
		return amazonConfig.getProfilePhoto() + '/' + uuid.getUuid();
	}

	public String generateSuggestionPhotoKeyName(Uuid uuid) {
		return amazonConfig.getSuggestionPhoto() + '/' + uuid.getUuid();
	}

	public String generateChatRoomImageKeyName(String roomId, Uuid uuid, String originalFilename) {
		LocalDate now = LocalDate.now(ZONE_SEOUL);

		String basePrefix = "chatRoomsImages"; // 필요 시 amazonConfig에서 읽어오도록 변경 가능
		String safe = safeFilename(originalFilename);

		// 확장자 분리
		String namePart = safe;
		String ext = "";
		int dot = safe.lastIndexOf('.');
		if (dot > -1) {
			namePart = safe.substring(0, dot);
			ext = safe.substring(dot); // ".jpg" 포함
		}

		return String.format("%s/%s/images/%04d/%02d/%02d/%s-%s%s",
			basePrefix, roomId,
			now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
			uuid.getUuid(), namePart, ext);
	}

	private String safeFilename(String filename) {
		if (filename == null || filename.isBlank())
			return "file";
		String cleaned = filename.replaceAll("\\s+", "_");
		return URLEncoder.encode(cleaned, StandardCharsets.UTF_8);
	}

	/**
	 * 서버에서 생성된 파일 스트림(InputStream)을 S3에 업로드합니다.
	 * @param keyName S3에 저장될 파일의 전체 경로 및 이름
	 * @param inputStream 업로드할 파일의 데이터 스트림
	 * @param contentLength 파일의 크기
	 * @param contentType 파일의 MIME 타입 (예: "application/pdf")
	 * @return 업로드된 파일의 URL
	 */
	public String uploadInputStream(String keyName, InputStream inputStream, long contentLength, String contentType) {
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(contentLength);
		metadata.setContentType(contentType);

		try {
			amazonS3.putObject(
				new PutObjectRequest(amazonConfig.getBucket(), keyName, inputStream, metadata)
			);
		} catch (Exception e) {
			log.error("S3에 InputStream 업로드 중 오류 발생 : {}", (Object)e.getStackTrace());
			throw new RuntimeException("S3에 파일 업로드 중 오류가 발생했습니다.", e);
		}

		return amazonS3.getUrl(amazonConfig.getBucket(), keyName).toString();
	}
}
