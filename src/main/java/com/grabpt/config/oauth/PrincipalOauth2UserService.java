package com.grabpt.config.oauth;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.grabpt.config.oauth.provider.GoogleUserInfo;
import com.grabpt.config.oauth.provider.KakaoUserInfo;
import com.grabpt.config.oauth.provider.NaverUserInfo;
import com.grabpt.config.oauth.provider.OAuth2UserInfo;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.AuthRole;
import com.grabpt.domain.enums.Role;
import com.grabpt.repository.UserRepository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

	private final UserRepository userRepository;

	// userRequest 는 code를 받아서 accessToken을 응답 받은 객체
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(userRequest); // google의 회원 프로필 조회

		// code를 통해 구성한 정보
		System.out.println("userRequest clientRegistration : " + userRequest.getClientRegistration());
		// token을 통해 응답받은 회원정보
		System.out.println("oAuth2User : " + oAuth2User);

		return processOAuth2User(userRequest, oAuth2User);
	}

	private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {

		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		String userNameAttributeName = userRequest.getClientRegistration()
			.getProviderDetails()
			.getUserInfoEndpoint()
			.getUserNameAttributeName();

		OAuth2UserInfo oAuth2UserInfo = switch (registrationId) {
			case "google" -> new GoogleUserInfo(oAuth2User.getAttributes());
			case "kakao" -> new KakaoUserInfo(oAuth2User.getAttributes());
			case "naver" -> new NaverUserInfo((Map)oAuth2User.getAttributes().get("response"));
			default -> throw new OAuth2AuthenticationException("지원하지 않는 로그인 방식입니다.");
		};

		String email = oAuth2UserInfo.getEmail();
		if (email == null || email.isBlank()) {
			email = UUID.randomUUID().toString() + "@oauth.com";
		}

		// providerId가 Long일 경우 대비
		String providerId = String.valueOf(oAuth2UserInfo.getProviderId());

		Optional<Users> userOptional =
			userRepository.findByOauthProviderAndOauthId(oAuth2UserInfo.getProvider(), oAuth2UserInfo.getProviderId());

		Users user = userOptional.orElse(
			Users.builder()
				.nickname(oAuth2UserInfo.getName())
				.password(UUID.randomUUID().toString()) // 일반 로그인 차단용 임시 비번
				.email(oAuth2UserInfo.getEmail())
				.role(Role.USER)
				.authRole(AuthRole.ROLE_USER)
				.oauthProvider(oAuth2UserInfo.getProvider())
				.oauthId(oAuth2UserInfo.getProviderId())
				.build()
		);

		return new DefaultOAuth2User(
			java.util.Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())),
			oAuth2User.getAttributes(),
			userNameAttributeName
		);
	}
}

