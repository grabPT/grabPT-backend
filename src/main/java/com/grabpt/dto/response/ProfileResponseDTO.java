package com.grabpt.dto.response;

import java.util.List;

import com.grabpt.domain.entity.Address;
import com.grabpt.domain.entity.ProPhoto;
import com.grabpt.domain.entity.PtPrice;
import com.grabpt.domain.entity.Review;
import com.grabpt.domain.entity.Users;

import lombok.Builder;
import lombok.Getter;


@Builder
@Getter
public class ProfileResponseDTO {

	// 일반 사용자용
	@Getter
	@Builder
	public static class MyProfileDTO {
		private Long userId;
		private String profileImageUrl;
		private String userName;
		private String userNickName;
		private String email;
		private String categoryName;
		private List<MyProProfileDTO.AddressDTO> address;

		@Getter
		@Builder
		public static class AddressDTO {
			private String city;
			private String district;
			private String street;
			private String zipcode;
			private String specAddress;

			public static MyProProfileDTO.AddressDTO from(Address address) {
				return MyProProfileDTO.AddressDTO.builder()
					.city(address.getCity())
					.district(address.getDistrict())
					.street(address.getStreet())
					.zipcode(address.getZipcode())
					.specAddress(address.getSpecAddress())
					.build();
			}
		}
	}

	// 전문가용
	@Getter
	@Builder
	public static class MyProProfileDTO {
		// 프로필 카드
		private Long userId;
		private String profileImageUrl;
		private String userName;
		private String userNickName;
		private String centerName;
		private String categoryName; // 카테고리 이름 추가
		private Double averageRating; // 리뷰 평점 추가

		private String profileDescription;
		private String centerDescription;

		// 소개 이미지
		private List<MyProProfileDTO.PhotoDTO> photos;


		// PT 프로그램 과정
		private Integer pricePerSession;
		private List<PtPrice> ptPrices;

		// location
		private List<AddressDTO> userLocations;

		@Getter
		@Builder
		public static class AddressDTO {
			private String city;
			private String district;
			private String street;
			private String zipcode;
			private String specAddress;

			public static AddressDTO from(Address address) {
				return AddressDTO.builder()
					.city(address.getCity())
					.district(address.getDistrict())
					.street(address.getStreet())
					.zipcode(address.getZipcode())
					.specAddress(address.getSpecAddress())
					.build();
			}
		}

		@Getter
		@Builder
		public static class ReviewDTO {
			private String reviewer;
			private Double rating;
			private String content;

			public static ReviewDTO from(Review review) {
				Users user = review.getUser();
				return ReviewDTO.builder()
					.reviewer(user.getNickname())
					.rating(review.getRating())
					.content(review.getContent())
					.build();
			}
		}

		@Getter
		@Builder
		public static class PhotoDTO {
			private String imageUrl;

			public static PhotoDTO from(ProPhoto photo) {
				return PhotoDTO.builder()
					.imageUrl(photo.getImageUrl())
					.build();
			}
		}
	}

}
