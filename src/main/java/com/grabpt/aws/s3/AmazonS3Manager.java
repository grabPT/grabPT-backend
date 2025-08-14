package com.grabpt.aws.s3;

import java.io.IOException;
import java.io.InputStream;

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

	public String generateContractPhotoKeyName(Uuid uuid){
		return amazonConfig.getContractPhoto() + '/' + uuid.getUuid();
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
