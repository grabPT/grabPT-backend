package com.grabpt.config.oauth.handler;

import static com.grabpt.config.jwt.properties.CookieSupport.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.grabpt.config.jwt.JwtTokenProvider;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.Role;
import com.grabpt.repository.UserRepository.UserRepository;

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

	private static String b64(String s) {
		if (s == null)
			return "";
		return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
	}

	/** 로컬에서만 쓰는 host-only 쿠키 헬퍼 */
	private static ResponseCookie localCookie(String name, String value, Duration maxAge, boolean httpOnly) {
		return ResponseCookie.from(name, value == null ? "" : value)
			.path("/")
			.maxAge(maxAge)
			.httpOnly(httpOnly)
			.secure(false)
			.sameSite("Lax")
			.build();
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {

		OAuth2User oAuth2User = (OAuth2User)authentication.getPrincipal();
		OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken)authentication;
		String oauthProvider = oauthToken.getAuthorizedClientRegistrationId();

		Map<String, Object> attributes = oAuth2User.getAttributes();
		String email = null, name = null, oauthId = null;
		if ("google".equals(oauthProvider)) {
			email = (String)attributes.get("email");
			name = (String)attributes.get("name");
			oauthId = oauthProvider + "-" + attributes.get("sub");
		} else if ("kakao".equals(oauthProvider)) {
			Map<String, Object> acc = (Map<String, Object>)attributes.get("kakao_account");
			Map<String, Object> profile = (Map<String, Object>)acc.get("profile");
			email = acc.get("email") != null ? (String)acc.get("email") : null;
			name = profile != null ? (String)profile.get("nickname") : null;
			oauthId = oauthProvider + "-" + attributes.get("id");
		} else if ("naver".equals(oauthProvider)) {
			Map<String, Object> resMap = (Map<String, Object>)attributes.get("response");
			email = resMap.get("email") != null ? (String)resMap.get("email") : null;
			name = resMap.get("name") != null ? (String)resMap.get("name") : null;
			oauthId = oauthProvider + "-" + resMap.get("id");
		}

		boolean isLocal = FrontendRoutingSupport.isLocal(request);
		Users oauthUser = userRepository.findByOauthProviderAndOauthId(oauthProvider, oauthId).orElse(null);

		if (oauthUser != null) {
			String accessToken = jwtTokenProvider.generateToken(oauthUser);
			String refreshToken = oauthUser.getRefreshToken();

			if (isLocal) {
				response.addHeader(HttpHeaders.SET_COOKIE,
					localCookie("ACCESS_TOKEN", accessToken, Duration.ofHours(1), true).toString());
				response.addHeader(HttpHeaders.SET_COOKIE,
					localCookie("REFRESH_TOKEN", refreshToken, Duration.ofDays(14), true).toString());
				response.addHeader(HttpHeaders.SET_COOKIE,
					localCookie("ROLE", b64(oauthUser.getRole() == Role.PRO ? "EXPERT" : oauthUser.getRole().name()),
						Duration.ofDays(7), false).toString());
				response.addHeader(HttpHeaders.SET_COOKIE,
					localCookie("USER_ID", b64(oauthUser.getId().toString()), Duration.ofDays(7), false).toString());
			} else {
				// 운영 정책은 기존 CookieSupport 그대로 사용
				response.addHeader(HttpHeaders.SET_COOKIE, accessCookie(accessToken).toString());
				response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie(refreshToken).toString());
				response.addHeader(HttpHeaders.SET_COOKIE, refreshCookieAtRoot(refreshToken).toString());
				response.addHeader(HttpHeaders.SET_COOKIE,
					roleCookie(
						b64(oauthUser.getRole() == Role.PRO ? "EXPERT" : oauthUser.getRole().name())).toString());
				response.addHeader(HttpHeaders.SET_COOKIE,
					userIdCookie(b64(oauthUser.getId().toString())).toString());
			}

			org.springframework.security.core.context.SecurityContextHolder.clearContext();
			HttpSession session = request.getSession(false);
			if (session != null)
				session.invalidate();

			String feBase = FrontendRoutingSupport.frontendBase(request);
			response.sendRedirect(feBase + "/authcallback");
			return;
		}

		// 신규 회원 임시 쿠키 – 요청 기반 분기
		FrontendRoutingSupport.addTempCookie(request, response, "oauthEmail", b64(email), 180, false);
		FrontendRoutingSupport.addTempCookie(request, response, "oauthName", b64(name), 180, false);
		FrontendRoutingSupport.addTempCookie(request, response, "oauthId", b64(oauthId), 180, false);
		FrontendRoutingSupport.addTempCookie(request, response, "oauthProvider", b64(oauthProvider), 180, false);

		HttpSession session = request.getSession();
		session.setAttribute("tempEmail", email);
		session.setAttribute("tempName", name);
		session.setAttribute("tempOauthProvider", oauthProvider);
		session.setAttribute("tempOauthId", oauthId);

		String feBase = FrontendRoutingSupport.frontendBase(request);
		response.sendRedirect(feBase + "/signup");
	}
}
