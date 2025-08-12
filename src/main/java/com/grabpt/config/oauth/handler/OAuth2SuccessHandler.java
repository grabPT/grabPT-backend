package com.grabpt.config.oauth.handler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.grabpt.config.jwt.JwtTokenProvider;
import com.grabpt.domain.entity.Users;
import com.grabpt.repository.UserRepository.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;

	private static final String SHARED_DOMAIN = "grabpt.com";
	private static final String FRONT_HOME = "https://www.grabpt.com/";
	private static final String FRONT_SIGNUP = "https://www.grabpt.com/signup";

	private static String b64(String s) {
		if (s == null)
			return "";
		return Base64.getEncoder()
			.encodeToString(s.getBytes(StandardCharsets.UTF_8));
	}

	private static void addCookie(HttpServletResponse res, String name, String value,
		Duration maxAge, boolean httpOnly) {
		ResponseCookie c = ResponseCookie.from(name, value == null ? "" : value)
			.domain(SHARED_DOMAIN)
			.path("/")
			.maxAge(maxAge)
			.secure(true)
			.httpOnly(httpOnly)
			.sameSite("None")
			.build();
		res.addHeader(HttpHeaders.SET_COOKIE, c.toString());
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {

		OAuth2User oAuth2User = (OAuth2User)authentication.getPrincipal();
		OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken)authentication;
		String oauthProvider = oauthToken.getAuthorizedClientRegistrationId();

		Map<String, Object> attributes = oAuth2User.getAttributes();
		String email = null;
		String name = null;
		String oauthId = null;

		if ("google".equals(oauthProvider)) {
			email = (String)attributes.get("email");
			name = (String)attributes.get("name");
			Object sub = attributes.get("sub");
			oauthId = oauthProvider + "-" + (sub == null ? "" : String.valueOf(sub));

		} else if ("kakao".equals(oauthProvider)) {
			Map<String, Object> kakaoAccount = (Map<String, Object>)attributes.get("kakao_account");
			Map<String, Object> profile =
				kakaoAccount == null ? null : (Map<String, Object>)kakaoAccount.get("profile");
			email = kakaoAccount != null ? (String)kakaoAccount.get("email") : null; // null 가능
			name = profile != null ? (String)profile.get("nickname") : null;

			Object idObj = attributes.get("id");
			oauthId = oauthProvider + "-" + (idObj == null ? "" : String.valueOf(idObj));

		} else if ("naver".equals(oauthProvider)) {
			Map<String, Object> resp = (Map<String, Object>)attributes.get("response");
			if (resp != null) {
				email = (String)resp.get("email");
				name = (String)resp.get("name");
				oauthId = oauthProvider + "-" + Objects.toString(resp.get("id"), "");
			}
		} else {
			log.warn("Unsupported oauthProvider: {}", oauthProvider);
		}

		log.info("[OAUTH SUCCESS] provider={}, oauthId={}, email={}, name={}",
			oauthProvider, oauthId, email, name);

		// === 1) (provider, oauthId) 기준으로 기존 회원 조회 ===
		Users byProviderId = null;
		if (oauthId != null && !oauthId.isEmpty()) {
			byProviderId = userRepository.findByOauthProviderAndOauthId(oauthProvider, oauthId).orElse(null);
		}

		if (byProviderId != null) {
			// 기존 소셜 연동 회원 → 토큰 발급 & 홈 이동
			String accessToken = jwtTokenProvider.generateToken(byProviderId);
			String refreshToken = jwtTokenProvider.createRefreshToken(byProviderId.getEmail());
			byProviderId.setRefreshToken(refreshToken);
			userRepository.save(byProviderId);

			addCookie(response, "accessToken", accessToken, Duration.ofMinutes(30), true);
			addCookie(response, "refreshToken", refreshToken, Duration.ofDays(7), true);

			response.sendRedirect(FRONT_HOME);
			return;
		}

		// === 2) 신규 가입 준비(세션 + 임시 쿠키 후 /signup으로) ===
		//  - 카카오는 email null일 수 있으니, 프론트에서 이메일 입력 받아 별도 가입 API로 마무리
		//  - 여기서는 registrationToken 쓰지 않고 세션으로만 운반
		HttpSession session = request.getSession();
		session.setAttribute("tempOauthProvider", oauthProvider);
		session.setAttribute("tempOauthId", oauthId);
		session.setAttribute("tempName", name);
		session.setAttribute("tempEmail", email); // null 허용

		// 프론트 읽기용 최소 정보(b64). 민감/신뢰 필요한 값은 세션에만.
		addCookie(response, "oauthName", b64(name), Duration.ofMinutes(10), false);
		addCookie(response, "oauthEmail", b64(email), Duration.ofMinutes(10), false);
		addCookie(response, "oauthProvider", b64(oauthProvider), Duration.ofMinutes(10), false);

		response.sendRedirect(FRONT_SIGNUP);
	}
}
