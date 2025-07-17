package com.grabpt.converter;


import com.grabpt.domain.entity.ProProfile;
import com.grabpt.domain.entity.UserProfile;
import com.grabpt.domain.entity.Users;
import com.grabpt.dto.response.ProProfileResponseDTO;
import com.grabpt.dto.response.ProfileResponseDTO;


import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ProfileConverter {

	// MyProfileDTO 변환
	public static ProfileResponseDTO.MyProfileDTO toMyProfileDTO(Users user) {
		UserProfile profile = user.getUserProfile();

		if (profile == null) {
			return ProfileResponseDTO.MyProfileDTO.builder()
				.userId(user.getId())
				.name(user.getUsername())
				.nickname(user.getNickname())
				.email(user.getEmail())
				.build();
		}

		return ProfileResponseDTO.MyProfileDTO.builder()
			.userId(user.getId())
			.name(user.getUsername())
			.nickname(user.getNickname())
			.email(user.getEmail())
			.build();
	}

	// MyProProfileDTO 변환
	public static ProfileResponseDTO.MyProProfileDTO toMyProProfileDTO(Users user) {
		ProProfile proProfile = user.getProProfile();

		if (proProfile == null) {
			return ProfileResponseDTO.MyProProfileDTO.builder()
				.proId(user.getId())
				.nickname(user.getNickname())
				.description(null)
				.photos(Collections.emptyList())
				.programDescription(proProfile.getProgramDescription())
				.pricePerSession(proProfile.getPricePerSession())
				.totalSessions(proProfile.getTotalSessions())
				.certifications(Collections.emptyList())
				.reviews(Collections.emptyList())
				.build();
		}

		List<ProfileResponseDTO.MyProProfileDTO.PhotoDTO> photoDTOS = proProfile.getPhotos().stream()
			.map(photo -> ProfileResponseDTO.MyProProfileDTO.PhotoDTO.builder()
				.imageUrl(photo.getImageUrl())
				.description(photo.getDescription())
				.build())
			.collect(Collectors.toList());

		List<ProfileResponseDTO.MyProProfileDTO.CertificationDTO> certificationDTOS = proProfile.getCertifications().stream()
			.map(certification -> ProfileResponseDTO.MyProProfileDTO.CertificationDTO.builder()
				.name(certification.getName())
				.issuer(certification.getIssuer())
				.issuedDate(certification.getIssuedDate())
				.build())
			.collect(Collectors.toList());

		List<ProfileResponseDTO.MyProProfileDTO.ReviewDTO> reviewDTOS = proProfile.getReviews().stream()
			.map(review -> ProfileResponseDTO.MyProProfileDTO.ReviewDTO.builder()
				.authorName(review.getUser().getNickname())
				.rating(review.getRating())
				.content(review.getContent())
				.build())
			.collect(Collectors.toList());

		return ProfileResponseDTO.MyProProfileDTO.builder()
			.proId(user.getId())
			.nickname(user.getNickname())
			.description(proProfile.getDescription())
			.photos(photoDTOS)
			.programDescription(proProfile.getProgramDescription())
			.pricePerSession(proProfile.getPricePerSession())
			.totalSessions(proProfile.getTotalSessions())
			.certifications(certificationDTOS)
			.reviews(reviewDTOS)
			.build();
	}

	// ProProfileDetailDTO 변환
	public static ProProfileResponseDTO toProProfileDetailDTO(Users user) {
		ProProfile proProfile = user.getProProfile();

		List<ProProfileResponseDTO.CertificationDTO> certificationDTOS = proProfile.getCertifications().stream()
			.map(ProProfileResponseDTO.CertificationDTO::from)
			.collect(Collectors.toList());


		List<ProProfileResponseDTO.PhotoDTO> photoDTOS = proProfile.getPhotos().stream()
			.map(ProProfileResponseDTO.PhotoDTO::from)
			.collect(Collectors.toList());

		return ProProfileResponseDTO.builder()
			.name(user.getNickname())
			.photos(photoDTOS)
			.introduction(proProfile.getDescription())
			.certifications(certificationDTOS)
			.programDescription(proProfile.getProgramDescription())
			.pricePerSession(proProfile.getPricePerSession())
			.totalSessions(proProfile.getTotalSessions())
			.build();
	}
}
