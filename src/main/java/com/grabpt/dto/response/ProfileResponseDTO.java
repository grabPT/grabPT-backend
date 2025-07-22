package com.grabpt.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;


import java.util.List;

import com.grabpt.domain.entity.Address;
import com.grabpt.domain.entity.Center;
import com.grabpt.domain.entity.ProPhoto;
import com.grabpt.domain.entity.Review;
import com.grabpt.domain.entity.Users;
import com.grabpt.dto.request.SignupRequest;

/**
 * 프로필 관련 응답 DTO들을 모아놓은 클래스입니다.
 */
@Builder
@Getter
public class ProfileResponseDTO {

	/**
	 * 내 정보 조회 응답 DTO (일반 사용자용)
	 */
	@Getter
	@Builder
	public static class MyProfileDTO {
		private Long userId;
		private String profileImageUrl;
		private String name;
		private String nickname;
		private String email;
	}

	/**
	 * 내 정보 조회 응답 DTO (전문가용)
	 */
	@Getter
	@Builder
	public static class MyProProfileDTO {
		// 프로필 카드
		private Long proId;
		private String profileImageUrl;
		private String proName;
		private String center;
		private String categoryName; // 카테고리 이름 추가
		private Double averageRating; // 리뷰 평점 추가


		private String description;
		// 소개 이미지
		private List<MyProProfileDTO.PhotoDTO> photos;

		private List<MyProProfileDTO.ReviewDTO> reviews;

		// PT 프로그램 과정
		private String programDescription;
		private Integer pricePerSession;
		private Integer totalSessions;

		// location
		private List<AddressDTO> address;

		@Getter
		@Builder
		public static class AddressDTO {
			private String city;
			private String district;
			private String street;
			private String zipcode;

			public static AddressDTO from(Address address) {
				return AddressDTO.builder()
					.city(address.getCity())
					.district(address.getDistrict())
					.street(address.getStreet())
					.zipcode(address.getZipcode())
					.build();
			}
		}

		@Getter
		@Builder
		public static class ReviewDTO {
			private String authorName;
			private Double rating;
			private String content;

			public static ReviewDTO from(Review review) {
				Users user = review.getUser();
				return ReviewDTO.builder()
					.authorName(user.getNickname())
					.rating(review.getRating())
					.content(review.getContent())
					.build();
			}
		}

		@Getter
		@Builder
		public static class PhotoDTO {
			private String imageUrl;
			private String description;

			public static PhotoDTO from(ProPhoto photo){
				return PhotoDTO.builder()
					.imageUrl(photo.getImageUrl())
					.description(photo.getDescription())
					.build();
			}
		}
	}

}
