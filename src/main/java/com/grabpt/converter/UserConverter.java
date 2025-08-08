package com.grabpt.converter;

import com.grabpt.domain.entity.Users;
import com.grabpt.dto.response.UserResponseDto;

public class UserConverter {

	public static UserResponseDto.UserInfoDTO toUserInfoDTO(Users user) {
		return UserResponseDto.UserInfoDTO.builder()
			.userId(user.getId())
			.username(user.getUsername())
			.nickname(user.getNickname())
			.email(user.getEmail())
			.role(user.getRole().toString())
			.address(UserResponseDto.AddressDto.builder()
				.city(user.getAddress().getCity())
				.district(user.getAddress().getDistrict())
				.street(user.getAddress().getStreet())
				.zipcode(user.getAddress().getZipcode())
				.streetCode(user.getAddress().getStreetCode())
				.specAddress(user.getAddress().getSpecAddress())
				.build())
			.build();
	}
}
