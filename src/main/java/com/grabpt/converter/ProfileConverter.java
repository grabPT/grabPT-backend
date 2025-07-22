package com.grabpt.converter;


import com.grabpt.domain.entity.ProProfile;
import com.grabpt.domain.entity.UserProfile;
import com.grabpt.domain.entity.Users;
import com.grabpt.dto.response.ProProfileResponseDTO;
import com.grabpt.dto.response.ProfileResponseDTO;
import com.grabpt.domain.entity.Review;

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
				.profileImageUrl(user.getProfileImageUrl())
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

		double averageRating = proProfile.getReviews().stream()
			.mapToDouble(Review::getRating)
			.average()
			.orElse(0.0); // 리뷰가 없으면 0.0을 반환

		if (proProfile == null) {
			return ProfileResponseDTO.MyProProfileDTO.builder()
				.proId(user.getId())
				.profileImageUrl(user.getProfileImageUrl())
				.proName(user.getUsername())
				.center(proProfile.getCenter())
				.categoryName(user.getProProfile().getCategory().getName())
				.averageRating(averageRating)
				.description(null)
				.photos(Collections.emptyList())
				.programDescription(proProfile.getProgramDescription())
				.pricePerSession(proProfile.getPricePerSession())
				.totalSessions(proProfile.getTotalSessions())
				.address(Collections.emptyList())
				.center(proProfile.getCenter())
				.build();
		}

		List<ProfileResponseDTO.MyProProfileDTO.PhotoDTO> photoDTOS = proProfile.getPhotos().stream()
			.map(photo -> ProfileResponseDTO.MyProProfileDTO.PhotoDTO.builder()
				.imageUrl(photo.getImageUrl())
				.description(photo.getDescription())
				.build())
			.collect(Collectors.toList());

		List<ProfileResponseDTO.MyProProfileDTO.AddressDTO> addressDTOS = (user.getAddress() != null)
			? Collections.singletonList(ProfileResponseDTO.MyProProfileDTO.AddressDTO.from(user.getAddress()))
			: Collections.emptyList();


		return ProfileResponseDTO.MyProProfileDTO.builder()
			.proId(user.getId())
			.profileImageUrl(user.getProfileImageUrl())
			.proName(user.getUsername())
			.center(proProfile.getCenter())
			.categoryName(user.getProProfile().getCategory().getName())
			.averageRating(averageRating)
			.description(proProfile.getDescription())
			.photos(photoDTOS)
			.programDescription(proProfile.getProgramDescription())
			.pricePerSession(proProfile.getPricePerSession())
			.totalSessions(proProfile.getTotalSessions())
			.address(addressDTOS)
			.center(proProfile.getCenter())
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

		List<ProfileResponseDTO.MyProProfileDTO.AddressDTO> addressDTOS = (user.getAddress() != null)
			? Collections.singletonList(ProfileResponseDTO.MyProProfileDTO.AddressDTO.from(user.getAddress()))
			: Collections.emptyList();

		return ProProfileResponseDTO.builder()
			.name(user.getNickname())
			.photos(photoDTOS)
			.introduction(proProfile.getDescription())
			.certifications(certificationDTOS)
			.programDescription(proProfile.getProgramDescription())
			.pricePerSession(proProfile.getPricePerSession())
			.totalSessions(proProfile.getTotalSessions())
			.center(proProfile.getCenter())
			.address(addressDTOS)
			.build();
	}

	public static ProProfileResponseDTO toProProfileDetailDTO(ProProfile proProfile) {
		Users user = proProfile.getUser();

		List<ProProfileResponseDTO.CertificationDTO> certificationDTOS = proProfile.getCertifications().stream()
			.map(ProProfileResponseDTO.CertificationDTO::from)
			.collect(Collectors.toList());

		List<ProProfileResponseDTO.PhotoDTO> photoDTOS = proProfile.getPhotos().stream()
			.map(ProProfileResponseDTO.PhotoDTO::from)
			.collect(Collectors.toList());

		List<ProfileResponseDTO.MyProProfileDTO.AddressDTO> addressDTOS = (user.getAddress() != null)
			? Collections.singletonList(ProfileResponseDTO.MyProProfileDTO.AddressDTO.from(user.getAddress()))
			: Collections.emptyList();

		return ProProfileResponseDTO.builder()
			.name(user.getNickname()) // User 객체에서 닉네임 가져오기
			.photos(photoDTOS)
			.introduction(proProfile.getDescription())
			.certifications(certificationDTOS)
			.programDescription(proProfile.getProgramDescription())
			.pricePerSession(proProfile.getPricePerSession())
			.totalSessions(proProfile.getTotalSessions())
			.center(proProfile.getCenter())
			.address(addressDTOS)
			.build();
	}
}
