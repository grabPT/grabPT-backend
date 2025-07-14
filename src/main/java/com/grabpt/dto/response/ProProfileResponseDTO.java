package com.grabpt.dto.response;

import com.grabpt.domain.entity.ProCertification;
import com.grabpt.domain.entity.ProPhoto;
import com.grabpt.domain.entity.ProProgram;
import com.grabpt.domain.entity.Review;
import com.grabpt.domain.entity.Users;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ProProfileResponseDTO {

	// 프로필 카드
	private String name;
	private List<String> location;
	private Integer price;

	// 소개 이미지
	private List<PhotoDTO> photos;

	// 소개 글
	private String introduction;

	// 자격 사항
	private List<CertificationDTO> certifications;

	// PT 프로그램 과정
	private List<ProgramDTO> programs;

	// 이용자 후기
	private List<ReviewDTO> reviews;


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
	public static class ProgramDTO {
		private String title;
		private String description;
		private Integer pricePerSession;
		private Integer totalSessions;

		public static ProgramDTO from(ProProgram program) {
			return ProgramDTO.builder()
				.title(program.getTitle())
				.description(program.getDescription())
				.pricePerSession(program.getPricePerSession())
				.totalSessions(program.getTotalSessions())
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
