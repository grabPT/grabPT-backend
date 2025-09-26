package com.grabpt.converter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.grabpt.domain.entity.ProProfile;
import com.grabpt.domain.entity.Review;
import com.grabpt.domain.entity.UserProfile;
import com.grabpt.domain.entity.Users;
import com.grabpt.dto.response.ProProfileResponseDTO;
import com.grabpt.dto.response.ProfileResponseDTO;

public class ProfileConverter {

	// MyProfileDTO 변환
	public static ProfileResponseDTO.MyProfileDTO toMyProfileDTO(Users user) {
		UserProfile profile = user.getUserProfile();

		List<ProfileResponseDTO.MyProProfileDTO.AddressDTO> addressDTOS = (user.getAddress() != null)
			? Collections.singletonList(ProfileResponseDTO.MyProfileDTO.AddressDTO.from(user.getAddress()))
			: Collections.emptyList();

		// categoryName 안전 처리
		String categoryName = null;
		if (profile != null && profile.getCategory() != null) {
			categoryName = profile.getCategory().getName();
		}

		// profile 여부와 관계없이 동일한 빌더 호출
		return ProfileResponseDTO.MyProfileDTO.builder()
			.userId(user.getId())
			.profileImageUrl(user.getProfileImageUrl())
			.userName(user.getUsername())
			.userNickName(user.getNickname())
			.email(user.getEmail())
			.address(addressDTOS)
			.categoryName(categoryName)  // 없으면 null 반환
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
				.userId(user.getId())
				.profileImageUrl(user.getProfileImageUrl())
				.userName(user.getUsername())
				.centerName(proProfile.getCenter())
				.categoryName(user.getProProfile().getCategory().getName())
				.averageRating(averageRating)
				.profileDescription(null)
				.photos(Collections.emptyList())
				.pricePerSession(proProfile.getPricePerSession())
				.ptPrices(proProfile.getPtPrices())
				.userLocations(Collections.emptyList())
				.centerName(proProfile.getCenter())
				.build();
		}

		List<ProfileResponseDTO.MyProProfileDTO.PhotoDTO> photoDTOS = proProfile.getPhotos().stream()
			.map(photo -> ProfileResponseDTO.MyProProfileDTO.PhotoDTO.builder()
				.imageUrl(photo.getImageUrl())
				.build())
			.collect(Collectors.toList());

		List<ProfileResponseDTO.MyProProfileDTO.AddressDTO> addressDTOS = (user.getAddress() != null)
			? Collections.singletonList(ProfileResponseDTO.MyProProfileDTO.AddressDTO.from(user.getAddress()))
			: Collections.emptyList();

		return ProfileResponseDTO.MyProProfileDTO.builder()
			.userId(user.getId())
			.profileImageUrl(user.getProfileImageUrl())
			.userNickName(user.getNickname())
			.userName(user.getUsername())
			.centerName(proProfile.getCenter())
			.categoryName(user.getProProfile().getCategory().getCode().toLowerCase())
			.averageRating(averageRating)
			.profileDescription(proProfile.getDescription())
			.photos(photoDTOS)
			.pricePerSession(proProfile.getPricePerSession())
			.ptPrices(proProfile.getPtPrices())
			.userLocations(addressDTOS)
			.centerDescription(proProfile.getCenterDescription())
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
			.userNickName(user.getNickname()) // User 객체에서 닉네임 가져오기
			.profileImageUrl(user.getProfileImageUrl())
			.photos(photoDTOS)
			.userId(user.getId())
			.introduction(proProfile.getDescription())
			.certifications(certificationDTOS)
			.programDescription(proProfile.getProgramDescription())
			.pricePerSession(proProfile.getPricePerSession())
			.totalSessions(proProfile.getTotalSessions())
			.ptPrices(proProfile.getPtPrices())
			.center(proProfile.getCenter())
			.userLocations(addressDTOS)
			.categoryName(proProfile.getCategory().getCode())
			.proCenterDescription(proProfile.getCenterDescription())
			.build();
	}
}
