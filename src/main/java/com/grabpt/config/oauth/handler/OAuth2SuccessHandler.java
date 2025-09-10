package com.grabpt.config.oauth.handler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

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

	// === D안: host[:port] → FE base 매핑 ===
	private static final java.util.Map<String, String> HOSTPORT_TO_FE = java.util.Map.of(
		// prod (표준 포트는 host만 들어오는 경우가 많으므로 별도 처리 아래에서 함께 함)
		"api.grabpt.com", "https://www.grabpt.com",
		// local / dev
		"localhost:5173", "http://localhost:5173",
		"localhost:8080", "http://localhost:8080",
		"182.216.71.74:8080", "http://182.216.71.74:8080",
		"192.168.1.101:3000", "http://192.168.1.101:3000"
	);

	private static String b64(String s) {
		if (s == null)
			return "";
		return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
	}

	// 로컬/운영에 맞춘 쿠키 속성 자동 분기
	private static void addCookieAdaptive(HttpServletRequest req, HttpServletResponse res,
		String name, String value, Duration maxAge, boolean httpOnly) {

		String proto = Optional.ofNullable(req.getHeader("X-Forwarded-Proto"))
			.orElse(req.isSecure() ? "https" : "http");
		String host = Optional.ofNullable(req.getHeader("X-Forwarded-Host"))
			.orElse(req.getServerName());

		boolean secure = "https".equalsIgnoreCase(proto);
		boolean isGrabpt = host != null && (host.equals("api.grabpt.com") || host.endsWith(".grabpt.com"));

		ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(name, value == null ? "" : value)
			.path("/")
			.maxAge(maxAge)
			.httpOnly(httpOnly)
			.sameSite(secure ? "None" : "Lax")
			.secure(secure);

		if (isGrabpt) {
			// 운영만 최상위 도메인 공유
			b.domain("grabpt.com");
		}
		res.addHeader(HttpHeaders.SET_COOKIE, b.build().toString());
	}

	// 요청에서 host:port 도출 → 매핑 → 기본값
	private static String resolveClientBase(HttpServletRequest req) {
		String xfHost = req.getHeader("X-Forwarded-Host");
		String xfProto = req.getHeader("X-Forwarded-Proto");
		String xfPort = req.getHeader("X-Forwarded-Port");

		String host = (xfHost != null && !xfHost.isBlank()) ? xfHost : req.getServerName();
		String proto = (xfProto != null && !xfProto.isBlank()) ? xfProto : (req.isSecure() ? "https" : "http");

		int portNum = req.getServerPort();
		if (xfPort != null && !xfPort.isBlank()) {
			try {
				portNum = Integer.parseInt(xfPort);
			} catch (NumberFormatException ignored) {
			}
		}

		// 표준 포트는 생략된 경우가 있어 host만도 한번 매핑 시도
		String hostPort = (portNum > 0) ? (host + ":" + portNum) : host;

		// 1) host:port 매핑 우선
		String mapped = HOSTPORT_TO_FE.get(hostPort);
		if (mapped != null)
			return mapped;

		// 2) host만으로도 한 번 시도 (운영 표준 포트 등)
		mapped = HOSTPORT_TO_FE.get(host);
		if (mapped != null)
			return mapped;

		// 3) 기본값 (운영 FE)
		return "https://www.grabpt.com";
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication)
		throws IOException, ServletException {

		OAuth2User oAuth2User = (OAuth2User)authentication.getPrincipal();
		OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken)authentication;
		String oauthProvider = oauthToken.getAuthorizedClientRegistrationId();
		Map<String, Object> attributes = oAuth2User.getAttributes();

		String email = null;
		String name = null;
		String oauthId = null;

		if (oauthProvider.equals("google")) {
			email = (String)attributes.get("email");
			name = (String)attributes.get("name");
			oauthId = oauthProvider + "-" + attributes.get("sub");
		} else if (oauthProvider.equals("kakao")) {
			Map<String, Object> kakaoAccount = (Map<String, Object>)attributes.get("kakao_account");
			Map<String, Object> profile = (Map<String, Object>)kakaoAccount.get("profile");
			email = kakaoAccount.get("email") != null ? (String)kakaoAccount.get("email") : null;
			name = profile != null ? (String)profile.get("nickname") : null;
			oauthId = oauthProvider + "-" + attributes.get("id");
		} else if (oauthProvider.equals("naver")) {
			Map<String, Object> responseMap = (Map<String, Object>)attributes.get("response");
			email = responseMap.get("email") != null ? (String)responseMap.get("email") : null;
			name = responseMap.get("name") != null ? (String)responseMap.get("name") : null;
			oauthId = oauthProvider + "-" + responseMap.get("id");
		}

		Users oauthUser = userRepository.findByOauthProviderAndOauthId(oauthProvider, oauthId).orElse(null);

		// D안: 요청의 host:port 기준으로 프론트 베이스 계산
		String clientBase = resolveClientBase(request);
		String donePath = "/authcallback";
		String signupPath = "/signup";

		if (oauthUser != null) {
			log.info("기존 존재 회원 로직 진입 - {}", oauthUser.getEmail());

			String accessToken = jwtTokenProvider.generateToken(oauthUser);
			String refreshToken = oauthUser.getRefreshToken();

			// 환경별 속성으로 쿠키 발급
			addCookieAdaptive(request, response, "Authorization", accessToken, Duration.ofHours(1), true);
			addCookieAdaptive(request, response, "Refresh-Token", refreshToken, Duration.ofDays(14), true);
			addCookieAdaptive(request, response, "role",
				b64(oauthUser.getRole() == Role.PRO ? "EXPERT" : oauthUser.getRole().name()), Duration.ofDays(7),
				false);
			addCookieAdaptive(request, response, "userId",
				b64(oauthUser.getId().toString()), Duration.ofDays(7), false);

			org.springframework.security.core.context.SecurityContextHolder.clearContext();
			var session = request.getSession(false);
			if (session != null)
				session.invalidate();

			response.sendRedirect(clientBase + donePath);
			return;
		}

		// 신규 회원: 임시 쿠키/세션
		addCookieAdaptive(request, response, "oauthEmail", b64(email), Duration.ofMinutes(3), false);
		addCookieAdaptive(request, response, "oauthName", b64(name), Duration.ofMinutes(3), false);
		addCookieAdaptive(request, response, "oauthId", b64(oauthId), Duration.ofMinutes(3), false);
		addCookieAdaptive(request, response, "oauthProvider", b64(oauthProvider), Duration.ofMinutes(3), false);

		HttpSession session = request.getSession();
		session.setAttribute("tempEmail", email);
		session.setAttribute("tempName", name);
		session.setAttribute("tempOauthProvider", oauthProvider);
		session.setAttribute("tempOauthId", oauthId);

		log.info("신규 회원 소셜 로그인 - provider: {}, email: {}, name: {}", oauthProvider, email, name);

		response.sendRedirect(clientBase + signupPath);
	}
}
