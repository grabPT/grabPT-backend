package com.grabpt.config.oauth.handler;

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
import com.grabpt.config.oauth.DynamicCookieSupport;
import com.grabpt.config.oauth.RedirectTargetResolver;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.Role;
import com.grabpt.repository.UserRepository.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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

	// ====== helpers ======

	private static String b64(String s) {
		if (s == null)
			return "";
		return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
	}

	/** request 기반으로 (로컬/운영) 자동 profile 적용한 Cookie Builder 반환 */
	private ResponseCookie.ResponseCookieBuilder cookie(String name, String value, HttpServletRequest req) {
		return DynamicCookieSupport.newCookie(name, value, req);
	}

	private void add(HttpServletResponse res, ResponseCookie c) {
		res.addHeader(HttpHeaders.SET_COOKIE, c.toString());
	}

	private static String getCookieValue(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null)
			return null;
		for (Cookie c : cookies) {
			if (name.equals(c.getName())) {
				return c.getValue();
			}
		}
		return null;
	}

	// ====== main ======

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
		HttpServletResponse response,
		Authentication authentication)
		throws IOException, ServletException {

		// 0) 최종 리다이렉트 대상(frontend base) 판별
		String redirectHint = getCookieValue(request, RedirectTargetResolver.REDIRECT_URI_COOKIE);
		String frontendBase = RedirectTargetResolver.resolveFrontendBase(request, redirectHint);
		if (!RedirectTargetResolver.isAllowedRedirectBase(frontendBase)) {
			log.warn("Blocked unexpected redirect base: {}", frontendBase);
			frontendBase = RedirectTargetResolver.EnvTarget.PROD_FE.base;
		}
		log.debug("[OAUTH][SUCCESS] frontendBase={}", frontendBase);

		// 1) 프로바이더 / 유저 정보 추출
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
			oauthId = oauthProvider + "-" + attributes.get("sub");
		} else if ("kakao".equals(oauthProvider)) {
			Map<String, Object> kakaoAccount = castMap(attributes.get("kakao_account"));
			Map<String, Object> profile = kakaoAccount == null ? null : castMap(kakaoAccount.get("profile"));
			email = kakaoAccount != null ? str(kakaoAccount.get("email")) : null;
			name = profile != null ? str(profile.get("nickname")) : null;
			oauthId = oauthProvider + "-" + attributes.get("id");
		} else if ("naver".equals(oauthProvider)) {
			Map<String, Object> resp = castMap(attributes.get("response"));
			email = resp != null ? str(resp.get("email")) : null;
			name = resp != null ? str(resp.get("name")) : null;
			oauthId = oauthProvider + "-" + (resp != null ? str(resp.get("id")) : null);
		}
		log.info("[OAUTH][SUCCESS] provider={} email={} name={}", oauthProvider, email, name);

		// 2) 기존 회원 여부
		Users oauthUser = userRepository.findByOauthProviderAndOauthId(oauthProvider, oauthId).orElse(null);

		if (oauthUser != null) {
			// ===== 기존 회원: 토큰 재발급 + /authcallback =====
			String accessToken = jwtTokenProvider.generateToken(oauthUser);
			String refreshToken = oauthUser.getRefreshToken();

			// HttpOnly 쿠키
			add(response, cookie("ACCESS_TOKEN", accessToken, request)
				.maxAge(Duration.ofHours(4))
				.build());
			add(response, cookie("REFRESH_TOKEN", refreshToken, request)
				.maxAge(Duration.ofDays(30))
				.build());

			// 프론트에서 읽을 공개 쿠키 (Base64)
			String roleStr = oauthUser.getRole() == Role.PRO ? "EXPERT" : oauthUser.getRole().name();
			add(response, cookie("ROLE", b64(roleStr), request)
				.httpOnly(false)
				.maxAge(Duration.ofDays(30))
				.build());
			add(response, cookie("USER_ID", b64(oauthUser.getId().toString()), request)
				.httpOnly(false)
				.maxAge(Duration.ofDays(30))
				.build());

			// 세션/컨텍스트 정리
			org.springframework.security.core.context.SecurityContextHolder.clearContext();
			HttpSession session = request.getSession(false);
			if (session != null)
				session.invalidate();

			response.sendRedirect(frontendBase + "/authcallback");
			return;
		}

		// ===== 신규 회원: 임시 쿠키(Base64, 공개) + 세션 보관 + /signup =====
		add(response, cookie("oauthEmail", b64(email), request)
			.httpOnly(false)
			.maxAge(Duration.ofMinutes(3))
			.build());
		add(response, cookie("oauthName", b64(name), request)
			.httpOnly(false)
			.maxAge(Duration.ofMinutes(3))
			.build());
		add(response, cookie("oauthId", b64(oauthId), request)
			.httpOnly(false)
			.maxAge(Duration.ofMinutes(3))
			.build());
		add(response, cookie("oauthProvider", b64(oauthProvider), request)
			.httpOnly(false)
			.maxAge(Duration.ofMinutes(3))
			.build());

		HttpSession session = request.getSession();
		session.setAttribute("tempEmail", email);
		session.setAttribute("tempName", name);
		session.setAttribute("tempOauthProvider", oauthProvider);
		session.setAttribute("tempOauthId", oauthId);

		response.sendRedirect(frontendBase + "/signup");
	}

	// ====== tiny utils ======
	@SuppressWarnings("unchecked")
	private static Map<String, Object> castMap(Object o) {
		if (o instanceof Map<?, ?> m)
			return (Map<String, Object>)m;
		return null;
	}

	private static String str(Object o) {
		return o == null ? null : String.valueOf(o);
	}
}
