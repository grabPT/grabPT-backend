package com.grabpt.service.CertificationService;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.grabpt.aws.s3.AmazonS3Manager;
import com.grabpt.aws.s3.Uuid;
import com.grabpt.domain.entity.ProCertification;
import com.grabpt.domain.entity.ProProfile;
import com.grabpt.dto.request.CertificationRequestDTO;
import com.grabpt.repository.UuidRepository.UuidRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CertificationServiceImpl implements CertificationService {

	private final AmazonS3Manager s3Manager;
	private final UuidRepository uuidRepository;

	@Override
	@Transactional
	public void updateCertifications(ProProfile proProfile, List<CertificationRequestDTO> certificationDTOs, List<MultipartFile> images) {

		proProfile.getCertifications().clear();

		if (certificationDTOs != null) {
			IntStream.range(0, certificationDTOs.size()).forEach(i -> {
				CertificationRequestDTO dto = certificationDTOs.get(i);
				MultipartFile image = (images != null && i < images.size()) ? images.get(i) : null;
				String imageUrl = null;

				if (image != null && !image.isEmpty()) {
					Uuid uuid = Uuid.builder().uuid(java.util.UUID.randomUUID().toString()).build();
					uuidRepository.save(uuid);
					String keyName = s3Manager.generateReviewKeyName(uuid);
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
