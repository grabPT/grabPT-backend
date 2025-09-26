package com.grabpt.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserResponseDto {

	@Builder
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class UserInfoDTO {
		Long userId;
		String userNickName;
		String userName;
		AddressDto address;
		String email;
		String role;
	}

	@Getter
	@Builder
	public static class AddressDto {
		private String city;
		private String district;
		private String street;
		private String zipcode;
		private String streetCode;
		private String specAddress;
	}

	@Getter
	@Builder
	public static class DuplicateEmailDto {
		private boolean isDuplicate;
		private String oauthProvider;
	}
}
