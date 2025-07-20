package com.grabpt.service.PhotoService;

import com.grabpt.aws.s3.AmazonS3Manager;
import com.grabpt.aws.s3.Uuid;
import com.grabpt.domain.entity.ProPhoto;
import com.grabpt.domain.entity.ProProfile;
import com.grabpt.domain.entity.Users;
import com.grabpt.repository.UuidRepository.UuidRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PhotoServiceImpl implements PhotoService {

	private final AmazonS3Manager amazonS3Manager;
	private final UuidRepository uuidRepository; // Uuid 저장을 위한 Repository

	@Override
	@Transactional
	public void updatePhotos(ProProfile proProfile, List<MultipartFile> photoFiles) {
		// 기존 사진 정보 모두 삭제
		proProfile.getPhotos().clear();

		if (photoFiles != null && !photoFiles.isEmpty()) {
			photoFiles.forEach(file -> {
				// 1. UUID 생성 및 S3 키 이름 생성
				Uuid uuid = Uuid.builder().uuid(java.util.UUID.randomUUID().toString()).build();
				uuidRepository.save(uuid);
				String keyName = amazonS3Manager.generateReviewKeyName(uuid);

				// 2. S3에 파일 업로드 후 URL 받기
				String fileUrl = amazonS3Manager.uploadFile(keyName, file);

				// 3. ProPhoto 엔티티 생성 및 ProProfile에 추가
				ProPhoto photo = ProPhoto.builder()
					.imageUrl(fileUrl)
					.description("") // 필요 시 설명을 받는 로직 추가
					.proProfile(proProfile)
					.build();
				proProfile.getPhotos().add(photo);
			});
		}

	}

	@Override
	@Transactional
	public String updateUserProfileImage(Users user, MultipartFile profileImage) {
		if (profileImage == null || profileImage.isEmpty()) {
			// 기존 이미지를 삭제하는 로직을 추가하거나, 그냥 반환할 수 있습니다.
			// 여기서는 기존 URL을 유지하도록 null을 반환합니다.
			return user.getProfileImageUrl();
		}

		// 1. UUID를 생성하여 S3에 저장될 파일의 고유 키를 만듭니다.
		Uuid uuid = Uuid.builder().uuid(java.util.UUID.randomUUID().toString()).build();
		uuidRepository.save(uuid);
		String keyName = amazonS3Manager.generateReviewKeyName(uuid); // proPhoto 경로를 재사용하거나 새 경로 생성

		// 2. S3에 파일을 업로드하고 URL을 받습니다.
		String fileUrl = amazonS3Manager.uploadFile(keyName, profileImage);

		// 3. Users 엔티티의 profileImageUrl 필드를 업데이트합니다.
		user.setProfileImageUrl(fileUrl);

		return fileUrl;
	}
}
