package com.grabpt.service.Impl;

import com.grabpt.domain.entity.ProPhoto;
import com.grabpt.domain.entity.ProProfile;
import com.grabpt.dto.request.PhotoRequestDTO;
import com.grabpt.service.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PhotoServiceImpl implements PhotoService {
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
