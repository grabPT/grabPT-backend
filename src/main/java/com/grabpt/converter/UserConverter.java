package com.grabpt.converter;

import com.grabpt.domain.entity.Users;
import com.grabpt.dto.response.UserResponseDto;

public class UserConverter {

	public static UserResponseDto.UserInfoDTO toUserInfoDTO(Users user) {
		return UserResponseDto.UserInfoDTO.builder()
			.username(user.getUsername())
			.nickname(user.getNickname())
			.address(user.getAddress())
			.email(user.getEmail())
			.userId(user.getId())
			.role(user.getRole().toString())
			.build();
	}
}
