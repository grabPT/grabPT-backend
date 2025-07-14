package com.grabpt.converter;


import com.grabpt.domain.entity.ProProfile;
import com.grabpt.domain.entity.ProProgram;
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
				.nickname(user.getNickname())
				.residence(null)
				.preferredAreas(Collections.emptyList())
				.categories(Collections.emptyList())
				.build();
		}

		return ProfileResponseDTO.MyProfileDTO.builder()
			.userId(user.getId())
			.nickname(user.getNickname())
			.residence(profile.getResidence())
			.preferredAreas(profile.getPreferredAreas())
			.categories(profile.getCategories().stream()
				.map(category -> category.getName())
				.collect(Collectors.toList()))
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
				.certifications(Collections.emptyList())
				.programs(Collections.emptyList())
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

		List<ProfileResponseDTO.MyProProfileDTO.ProgramDTO> programDTOS = proProfile.getPrograms().stream()
			.map(program -> ProfileResponseDTO.MyProProfileDTO.ProgramDTO.builder()
				.title(program.getTitle())
				.description(program.getDescription())
				.pricePerSession(program.getPricePerSession())
				.totalSessions(program.getTotalSessions())
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
			.certifications(certificationDTOS)
			.programs(programDTOS)
			.reviews(reviewDTOS)
			.build();
	}

	// ProProfileDetailDTO 변환
	public static ProProfileResponseDTO toProProfileDetailDTO(Users user) {
		ProProfile proProfile = user.getProProfile();

		List<ProProfileResponseDTO.CertificationDTO> certificationDTOS = proProfile.getCertifications().stream()
			.map(ProProfileResponseDTO.CertificationDTO::from)
			.collect(Collectors.toList());

		List<ProProfileResponseDTO.ProgramDTO> programDTOS = proProfile.getPrograms().stream()
			.map(ProProfileResponseDTO.ProgramDTO::from)
			.collect(Collectors.toList());

		List<ProProfileResponseDTO.ReviewDTO> reviewDTOS = proProfile.getReviews().stream()
			.map(ProProfileResponseDTO.ReviewDTO::from)
			.collect(Collectors.toList());

		List<ProProfileResponseDTO.PhotoDTO> photoDTOS = proProfile.getPhotos().stream()
			.map(ProProfileResponseDTO.PhotoDTO::from)
			.collect(Collectors.toList());

		return ProProfileResponseDTO.builder()
			.name(user.getNickname())
			.location(proProfile.getActivityAreas())
			.price(proProfile.getPrograms().stream()
				.map(ProProgram::getPricePerSession)
				.findFirst()
				.orElse(null))
			.photos(photoDTOS)
			.introduction(proProfile.getDescription())
			.certifications(certificationDTOS)
			.programs(programDTOS)
			.reviews(reviewDTOS)
			.build();
	}
}
