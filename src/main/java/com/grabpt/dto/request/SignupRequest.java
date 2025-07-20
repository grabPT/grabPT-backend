package com.grabpt.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
public class SignupRequest {

	@Builder
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class UserSignupRequestDto {
		@Schema(description = "oauth로 인증되어 가져온 이름", example = "홍길동")
		private String username;

		@Schema(description = "oauth로 인증되어 가져온 이메일", example = "email@gmail.com")
		private String email;

		@Schema(description = "사용자 전화번호", example = "01012345678")
		private String phoneNum;

		@Schema(description = "사용자가 지정한 이름", example = "동이")
		private String nickname;

		@Schema(description = "사용자가 입력한 비밇번호", example = "expasswd")
		private String password;

		@Schema(description = "주소 객체")
		private List<AddressRequest> address;

		@Schema(description = "카테고리 리스트", example = "[1, 2, 3]")
		private List<Long> categories;

		@Schema(description = "사용자가 입력한 프로필 이미지", example = "eoiaIOQJ2414kldsfPOVMIasd.png")
		private String profileImageUrl;

		@Schema(description = "oauth 인증 id", example = "google-12523512352351")
		private String oauthId;

		@Schema(description = "oauth 인증 provider", example = "google")
		private String oauthProvider;

		@Schema(description = "사용자가 입력한 성별, 1:남성/2:여성", example = "1")
		private Integer gender;

		@Schema(description = "일반 사용자, 트레이너 입력, 1:일반(USER)/2:트레이너(PRO)", example = "1")
		private Integer role;

		@Data
		public static class AddressRequest {
			@Schema(description = "도/특별시/광역시", example = "서울시")
			private String city;
			@Schema(description = "시/군/구", example = "관악구")
			private String district;
			@Schema(description = "읍/면/동", example = "봉천동")
			private String street;
			@Schema(description = "우편번호", example = "12345")
			private String zipcode;
		}
	}

	@Builder
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ProSignupRequestDto {
		@Schema(description = "oauth로 인증되어 가져온 이름", example = "홍길동")
		private String username;

		@Schema(description = "oauth로 인증되어 가져온 이메일", example = "email@gmail.com")
		private String email;

		@Schema(description = "사용자 전화번호", example = "01012345678")
		private String phoneNum;

		@Schema(description = "사용자가 지정한 이름", example = "동이")
		private String nickname;

		@Schema(description = "사용자가 입력한 비밇번호", example = "expasswd")
		private String password;

		@Schema(description = "주소 객체")
		private List<UserSignupRequestDto.AddressRequest> address;

		@Schema(description = "카테고리(프로는 하나)", example = "1")
		private Long categoryId;

		@Schema(description = "사용자가 입력한 프로필 이미지", example = "eoiaIOQJ2414kldsfPOVMIasd.png")
		private String profileImageUrl;

		@Schema(description = "oauth 인증 id", example = "google-12523512352351")
		private String oauthId;

		@Schema(description = "oauth 인증 provider", example = "google")
		private String oauthProvider;

		@Schema(description = "사용자가 입력한 성별, 1:남성/2:여성", example = "1")
		private Integer gender;

		@Schema(description = "일반 사용자, 트레이너 입력, 1:일반(USER)/2:트레이너(PRO)", example = "2")
		private Integer role;

		@Data
		public static class AddressRequest {
			@Schema(description = "도/특별시/광역시", example = "서울시")
			private String city;
			@Schema(description = "시/군/구", example = "관악구")
			private String district;
			@Schema(description = "읍/면/동", example = "봉천동")
			private String street;
			@Schema(description = "우편번호", example = "12345")
			private String zipcode;
		}

		@Schema(description = "활동지역")
		private List<String> activityAreas;

		@Schema(description = "센터")
		private String center;

		@Schema(description = "연차", example = "3")
		private Integer career;

		@Schema(description = "소개", example = "안녕하세요. 트레이너 홍길동입니다.")
		private String description;
	}

}
