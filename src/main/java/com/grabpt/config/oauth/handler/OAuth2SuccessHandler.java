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
import com.grabpt.config.oauth.support.RedirectTargetResolver;
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

	// ===== helpers =====

	private static String b64(String s) {
		if (s == null)
			return "";
		return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
	}

	private static String str(Object o) {
		return o == null ? null : String.valueOf(o);
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> castMap(Object o) {
		return (o instanceof Map<?, ?> m) ? (Map<String, Object>)m : null;
	}

	private static String getCookieValue(HttpServletRequest request, String... names) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null)
			return null;
		for (String n : names) {
			for (Cookie c : cookies)
				if (n.equals(c.getName()))
					return c.getValue();
		}
		return null;
	}

	private void addCookie(HttpServletResponse res, ResponseCookie c) {
		res.addHeader(HttpHeaders.SET_COOKIE, c.toString());
	}

	private static boolean isLocalTarget(String base) {
		if (base == null)
			return false;
		String b = base.toLowerCase();
		return b.startsWith("http://localhost:")
			|| b.startsWith("http://127.0.0.1")
			|| b.startsWith("http://0.0.0.0")
			|| b.startsWith("http://[::1]");
	}

	private static String jsQuote(String s) {
		if (s == null)
			return "\"\"";
		return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
	}

	private void bridgeRedirectIfLocal(HttpServletResponse response, String target) throws IOException {
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/html; charset=UTF-8");
		String html = """
			<!doctype html>
			<meta http-equiv="refresh" content="0;url='%s'">
			<script>location.replace(%s);</script>
			""".formatted(target, jsQuote(target));
		response.getWriter().write(html);
		response.getWriter().flush();
	}

	// ===== main =====

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
		HttpServletResponse response,
		Authentication authentication)
		throws IOException, ServletException {

		// 0) 최종 리다이렉트 베이스 결정: 세션 → 쿠키(redirect_uri / redirect_uri_hint) → 헤더 추론
		HttpSession session = request.getSession(false);
		String sessionHint = session == null ? null :
			(String)session.getAttribute(RedirectTargetResolver.REDIRECT_URI_COOKIE);
		String cookieHint = getCookieValue(request,
			RedirectTargetResolver.REDIRECT_URI_COOKIE,
			RedirectTargetResolver.ALT_REDIRECT_URI_COOKIE);

		String frontendBase = RedirectTargetResolver.resolveFrontendBase(
			request, sessionHint != null ? sessionHint : cookieHint);
		if (!RedirectTargetResolver.isAllowedRedirectBase(frontendBase)) {
			log.warn("Blocked unexpected redirect base: {}", frontendBase);
			frontendBase = RedirectTargetResolver.EnvTarget.PROD_FE.base;
		}
		log.debug("[OAUTH][SUCCESS] frontendBase={} (sessionHint={}, cookieHint={})",
			frontendBase, sessionHint, cookieHint);

		// 1) 공급자/프로필 파싱
		OAuth2User oAuth2User = (OAuth2User)authentication.getPrincipal();
		OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken)authentication;
		String oauthProvider = oauthToken.getAuthorizedClientRegistrationId();

		Map<String, Object> attributes = oAuth2User.getAttributes();
		String email = null, name = null, oauthId = null;

		if ("google".equals(oauthProvider)) {
			email = str(attributes.get("email"));
			name = str(attributes.get("name"));
			oauthId = oauthProvider + "-" + str(attributes.get("sub"));
		} else if ("kakao".equals(oauthProvider)) {
			Map<String, Object> kakaoAccount = castMap(attributes.get("kakao_account"));
			Map<String, Object> profile = kakaoAccount == null ? null : castMap(kakaoAccount.get("profile"));
			email = kakaoAccount != null ? str(kakaoAccount.get("email")) : null;
			name = profile != null ? str(profile.get("nickname")) : null;
			oauthId = oauthProvider + "-" + str(attributes.get("id"));
		} else if ("naver".equals(oauthProvider)) {
			Map<String, Object> resp = castMap(attributes.get("response"));
			email = resp != null ? str(resp.get("email")) : null;
			name = resp != null ? str(resp.get("name")) : null;
			oauthId = oauthProvider + "-" + (resp != null ? str(resp.get("id")) : null);
		}

		// 2) 기존 회원 여부
		Users oauthUser = userRepository.findByOauthProviderAndOauthId(oauthProvider, oauthId).orElse(null);

		if (oauthUser != null) {
			// === 기존 회원: 액세스 발급 + 리프레시 회전(항상 새로) ===
			String accessToken = jwtTokenProvider.generateToken(oauthUser);

			// refresh는 항상 회전 (JWT). 이메일이 null 가능하면 대체 subject 사용 고려.
			String emailForRefresh = oauthUser.getEmail() != null ? oauthUser.getEmail() : email;
			String newRefreshToken = jwtTokenProvider.createRefreshToken(emailForRefresh);

			// DB 저장
			oauthUser.setRefreshToken(newRefreshToken);
			userRepository.save(oauthUser);

			// HttpOnly 토큰 쿠키
			addCookie(response, DynamicCookieSupport.newCookie("ACCESS_TOKEN", accessToken, request)
				.maxAge(Duration.ofHours(4)).build());
			addCookie(response, DynamicCookieSupport.newCookie("REFRESH_TOKEN", newRefreshToken, request)
				.maxAge(Duration.ofDays(30)).build());

			// 공개 쿠키 (프론트에서 읽음)
			String roleStr = oauthUser.getRole() == Role.PRO ? "EXPERT" : oauthUser.getRole().name();
			addCookie(response, DynamicCookieSupport.asPublic(
					DynamicCookieSupport.newCookie("ROLE", b64(roleStr), request))
				.maxAge(Duration.ofDays(30)).build());
			addCookie(response, DynamicCookieSupport.asPublic(
					DynamicCookieSupport.newCookie("USER_ID", b64(oauthUser.getId().toString()), request))
				.maxAge(Duration.ofDays(30)).build());

			// 세션/컨텍스트 정리
			org.springframework.security.core.context.SecurityContextHolder.clearContext();
			if (session != null)
				session.invalidate();

			// 최종 이동: 로컬은 브리지(200 HTML → JS redirect), 운영은 302
			String target = frontendBase + "/authcallback";
			if (isLocalTarget(frontendBase)) {
				bridgeRedirectIfLocal(response, target);
			} else {
				response.sendRedirect(target);
			}
			return;
		}

		// === 신규 회원: 임시 공개 쿠키 3분 + 세션 보관 + /signup 이동 ===
		addCookie(response, DynamicCookieSupport.asPublic(
				DynamicCookieSupport.newCookie("oauthEmail", b64(email), request))
			.maxAge(Duration.ofMinutes(3)).build());
		addCookie(response, DynamicCookieSupport.asPublic(
				DynamicCookieSupport.newCookie("oauthName", b64(name), request))
			.maxAge(Duration.ofMinutes(3)).build());
		addCookie(response, DynamicCookieSupport.asPublic(
				DynamicCookieSupport.newCookie("oauthId", b64(oauthId), request))
			.maxAge(Duration.ofMinutes(3)).build());
		addCookie(response, DynamicCookieSupport.asPublic(
				DynamicCookieSupport.newCookie("oauthProvider", b64(oauthProvider), request))
			.maxAge(Duration.ofMinutes(3)).build());

		if (session == null)
			session = request.getSession(true);
		session.setAttribute("tempEmail", email);
		session.setAttribute("tempName", name);
		session.setAttribute("tempOauthProvider", oauthProvider);
		session.setAttribute("tempOauthId", oauthId);

		String target = frontendBase + "/signup";
		if (isLocalTarget(frontendBase)) {
			bridgeRedirectIfLocal(response, target);
		} else {
			response.sendRedirect(target);
		}
	}
}
