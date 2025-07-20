package com.grabpt.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.grabpt.domain.entity.ProCertification;
import com.grabpt.domain.entity.ProPhoto;
import com.grabpt.domain.entity.Review;
import com.grabpt.domain.entity.Users;

import lombok.Builder;
import lombok.Getter;

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
		private String nickname;
		private String description;
		private String center;

		// 소개 이미지
		private List<MyProProfileDTO.PhotoDTO> photos;

		private List<MyProProfileDTO.ReviewDTO> reviews;

		private List<MyProProfileDTO.CertificationDTO> certifications;
		// PT 프로그램 과정
		private String programDescription;
		private Integer pricePerSession;
		private Integer totalSessions;

		// location

		@Getter
		@Builder
		public static class CertificationDTO {
			private String name;
			private String issuer;
			private LocalDateTime issuedDate;

			public static CertificationDTO from(ProCertification certification) {
				return CertificationDTO.builder()
					.name(certification.getName())
					.issuer(certification.getIssuer())
					.issuedDate(certification.getIssuedDate())
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

			public static PhotoDTO from(ProPhoto photo) {
				return PhotoDTO.builder()
					.imageUrl(photo.getImageUrl())
					.description(photo.getDescription())
					.build();
			}
		}
	}

}
