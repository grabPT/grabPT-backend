package com.grabpt.service.PhotoService;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grabpt.aws.s3.AmazonS3Manager;
import com.grabpt.domain.entity.ProPhoto;
import com.grabpt.domain.entity.ProProfile;
import com.grabpt.dto.request.PhotoRequestDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PhotoServiceImpl implements PhotoService {

	private final AmazonS3Manager s3Manager;


	@Override
	@Transactional
	public void updatePhotos(ProProfile proProfile, List<PhotoRequestDTO> photoDTOs) {
		proProfile.getPhotos().clear();
		if (photoDTOs != null) {
			photoDTOs.forEach(dto -> {
				ProPhoto photo = ProPhoto.builder()
					.imageUrl(dto.getImageUrl())
					.description(dto.getDescription())
					.proProfile(proProfile)
					.build();
				proProfile.getPhotos().add(photo);
			});
		}
	}
}
