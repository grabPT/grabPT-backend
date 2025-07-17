package com.grabpt.service.UserService;

import java.util.Optional;

import com.grabpt.domain.entity.Users;
import com.grabpt.dto.response.UserResponseDto;

import jakarta.servlet.http.HttpServletRequest;

public interface UserQueryService {
	UserResponseDto.UserInfoDTO getUserInfo(HttpServletRequest request) throws IllegalAccessException;

	Optional<Users> findByEmail(String email);

	Long getUserId(HttpServletRequest request)  throws IllegalAccessException;

	void save(Users user);

	boolean existsByNickname(String nickname);
}
