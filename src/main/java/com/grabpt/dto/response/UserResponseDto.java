package com.grabpt.dto.response;

import com.grabpt.domain.entity.Address;

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
		String nickname;
		String username;
		Address address;
		String email;
		String role;
	}
}
