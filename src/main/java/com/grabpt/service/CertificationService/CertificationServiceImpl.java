package com.grabpt.service.CertificationService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.UserHandler;
import com.grabpt.aws.s3.AmazonS3Manager;
import com.grabpt.aws.s3.Uuid;
import com.grabpt.domain.entity.ProCertification;
import com.grabpt.domain.entity.ProProfile;
import com.grabpt.dto.request.CertificationRequestDTO;
import com.grabpt.dto.request.CertificationUpdateRequestDTO;
import com.grabpt.dto.request.ExistingCertificationDTO;
import com.grabpt.dto.request.NewCertificationDTO;
import com.grabpt.repository.UuidRepository.UuidRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CertificationServiceImpl implements CertificationService {

	private final AmazonS3Manager s3Manager;
	private final UuidRepository uuidRepository;

	@Override
	@Transactional
	public void updateCertifications(ProProfile proProfile, CertificationUpdateRequestDTO request,
		List<MultipartFile> newImages) {

		// 클라이언트가 유지하겠다고 보낸 기존 이미지 URL 목록
		Set<String> urlsToKeep = request.getExistingCertifications().stream()
			.map(ExistingCertificationDTO::getImageUrl)
			.collect(Collectors.toSet());

		// DB에 저장된 자격증 중, 유지 목록에 없는 것은 삭제
		proProfile.getCertifications().removeIf(cert -> !urlsToKeep.contains(cert.getImageUrl()));

		/// 유지하기로 한 기존 자격증들의 정보(설명 등) 업데이트
		Map<String, ExistingCertificationDTO> existingCertMap = request.getExistingCertifications().stream()
			.collect(Collectors.toMap(ExistingCertificationDTO::getImageUrl, dto -> dto));

		proProfile.getCertifications().forEach(cert -> {
			ExistingCertificationDTO dto = existingCertMap.get(cert.getImageUrl());
			if (dto != null) {
				cert.setDescription(dto.getDescription());
				cert.setCertificationType(dto.getCertificationType());
			}
		});

		// 새로 추가된 이미지들을 업로드하고 DB에 저장
		if (request.getNewCertifications() != null && newImages != null) {
			IntStream.range(0, request.getNewCertifications().size()).forEach(i -> {
				NewCertificationDTO dto = request.getNewCertifications().get(i);
				MultipartFile image = newImages.get(i);
				String imageUrl = null;

				if (image != null && !image.isEmpty()) {
					Uuid uuid = Uuid.builder().uuid(java.util.UUID.randomUUID().toString()).build();
					uuidRepository.save(uuid);
					String keyName = s3Manager.generateProPhotoKeyName(uuid);
					imageUrl = s3Manager.uploadFile(keyName, image);
				}

				ProCertification certification = ProCertification.builder()
					.certificationType(dto.getCertificationType())
					.description(dto.getDescription())
					.imageUrl(imageUrl)
					.proProfile(proProfile)
					.build();

				proProfile.getCertifications().add(certification);
			});
		}
	}
}
