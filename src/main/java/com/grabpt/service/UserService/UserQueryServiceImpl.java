package com.grabpt.service.UserService;

import java.util.Optional;

import org.apache.catalina.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.UserHandler;
import com.grabpt.config.auth.PrincipalDetails;
import com.grabpt.config.jwt.JwtTokenProvider;
import com.grabpt.converter.UserConverter;
import com.grabpt.domain.entity.Users;
import com.grabpt.dto.response.UserResponseDto;
import com.grabpt.repository.UserRepository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserQueryServiceImpl implements UserQueryService {

	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;

	@Override
	@Transactional(readOnly = true)
	public UserResponseDto.UserInfoDTO getUserInfo(HttpServletRequest request) throws IllegalAccessException {
		Authentication authentication = jwtTokenProvider.extractAuthentication(request);
		log.info("authentication = " + authentication);
		String email = ((PrincipalDetails)authentication.getPrincipal()).getUser().getEmail();

		Users user = userRepository.findByEmail(email)
			.orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));
		return UserConverter.toUserInfoDTO(user);
	}

	@Override
	@Transactional
	public Long getUserId(HttpServletRequest request) throws IllegalAccessException {
		Authentication authentication = jwtTokenProvider.extractAuthentication(request);
		Long userId = ((PrincipalDetails) authentication.getPrincipal()).getUser().getId();
		if(userRepository.existsById(userId)){
			return userId;
		} else{
			throw new UserHandler(ErrorStatus.MEMBER_NOT_FOUND);
		}
	}

	@Override
	public Optional<Users> findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public void save(Users user) {
		userRepository.save(user);
	}

	@Override
	public boolean existsByNickname(String nickname) {
		return userRepository.existsByNickname(nickname);
	}
}
