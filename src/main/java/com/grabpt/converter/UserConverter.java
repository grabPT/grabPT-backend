package com.grabpt.converter;

import com.grabpt.domain.entity.Users;
import com.grabpt.dto.response.UserResponseDto;

public class UserConverter {

	public static UserResponseDto.UserInfoDTO toUserInfoDTO(Users user) {
		return UserResponseDto.UserInfoDTO.builder()
			.name(user.getNickname())
			.email(user.getEmail())
			.build();
	}
}
