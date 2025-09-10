package com.grabpt.config.oauth;

import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.util.SerializationUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CookieUtils {

	// 운영 도메인 고정(상황에 따라 "grabpt.com"으로 올려도 되지만,
	// OAuth state는 api 호스트에서만 쓰이므로 api.grabpt.com 으로 명시하는 게 안전함)
	private static final String PROD_COOKIE_DOMAIN_FOR_OAUTH = "api.grabpt.com";

	// 기존 코드 유지: 다른 곳에서 null이면 host-only로 발급됨
	private static final String COOKIE_DOMAIN = null; // 앞에 점(.) 금지

	// ----- 환경 판단 (request 없이 동작해야 하므로 profile 기반) -----
	private static boolean isProdProfile() {
		// System property 우선
		String sp = System.getProperty("spring.profiles.active", "");
		if (sp != null && sp.toLowerCase().contains("prod"))
			return true;
		// Env var도 체크
		String ev = System.getenv("SPRING_PROFILES_ACTIVE");
		return ev != null && ev.toLowerCase().contains("prod");
	}

	// 간단 로깅용
	private static String abbr(String s) {
		if (s == null)
			return "null";
		return s.substring(0, Math.min(24, s.length())) + (s.length() > 24 ? "..." : "");
	}

	// ----- 읽기 -----
	public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			log.debug("[COOKIE][GET] reqUri={} -> no cookies", request.getRequestURI());
			return Optional.empty();
		}
		log.debug("[COOKIE][GET] reqUri={} cookieCount={}", request.getRequestURI(), cookies.length);
		Cookie best = null;
		for (Cookie c : cookies) {
			if (name.equals(c.getName())) {
				String v = c.getValue();
				log.debug("[COOKIE][GET] candidate name={} len={} prefix={}", c.getName(),
					v == null ? 0 : v.length(), v == null ? "null" : abbr(v));
				if (v != null && !v.isBlank() && (best == null || v.length() > best.getValue().length())) {
					best = c;
				}
			}
		}
		if (best != null) {
			log.debug("[COOKIE][GET] -> picked name={} len={} prefix={}",
				best.getName(), best.getValue().length(), abbr(best.getValue()));
		} else {
			log.debug("[COOKIE][GET] -> {} not found", name);
		}
		return Optional.ofNullable(best);
	}

	// ----- 쓰기 (공통) -----

	/**
	 * 일반 쿠키 발급(기존 정책 유지): SameSite=Lax, Secure=true, host-only or COOKIE_DOMAIN.
	 * ※ OAuth state/redirect 용은 아래 addOAuthStateCookie() 사용.
	 */
	public static void addCookie(HttpServletResponse response, String name, String value, int maxAgeSeconds) {
		ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(name, value)
			.path("/")
			.httpOnly(true)
			.secure(true)             // 기존 코드 유지
			.sameSite("Lax")          // 기존 코드 유지
			.maxAge(Duration.ofSeconds(maxAgeSeconds));

		if (COOKIE_DOMAIN != null)
			b.domain(COOKIE_DOMAIN);

		var built = b.build();
		var header = built.toString();
		log.debug("[COOKIE][ADD] name={} len={} domain={} path={} sameSite={} secure={} httpOnly={} maxAge={} -> {}",
			name, value == null ? 0 : value.length(),
			built.getDomain(), built.getPath(), built.getSameSite(), built.isSecure(),
			built.isHttpOnly(), built.getMaxAge().getSeconds(), header);
		response.addHeader(HttpHeaders.SET_COOKIE, header);
	}

	/**
	 * ✅ OAuth 상태 보관용 쿠키 전용 발급.
	 * - prod: SameSite=None; Secure; Domain=api.grabpt.com
	 * - dev/local: SameSite=Lax;  Secure=false; (domain 미지정)
	 *
	 * HttpCookieOAuth2AuthorizationRequestRepository 가 저장하는
	 *  - oauth2_auth_request
	 *  - redirect_uri
	 * 용도로 사용하면 브라우저에서 누락되지 않음.
	 */
	public static void addOAuthStateCookie(HttpServletResponse response, String name, String value, int maxAgeSeconds) {
		boolean prod = isProdProfile();

		ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(name, value)
			.path("/")
			.httpOnly(true)
			.maxAge(Duration.ofSeconds(maxAgeSeconds));

		if (prod) {
			b.secure(true)
				.sameSite("None")
				.domain(PROD_COOKIE_DOMAIN_FOR_OAUTH); // api.grabpt.com 으로 명시
		} else {
			b.secure(false)
				.sameSite("Lax"); // 로컬은 http가 많으므로 Lax
			// domain 미지정(Host-only)
		}

		var built = b.build();
		var header = built.toString();
		log.debug(
			"[COOKIE][ADD][OAUTH] name={} len={} domain={} path={} sameSite={} secure={} httpOnly={} maxAge={} -> {}",
			name, value == null ? 0 : value.length(),
			built.getDomain(), built.getPath(), built.getSameSite(), built.isSecure(),
			built.isHttpOnly(), built.getMaxAge().getSeconds(), header);
		response.addHeader(HttpHeaders.SET_COOKIE, header);
	}

	// ----- 삭제 -----
	private static void addDeletion(HttpServletResponse response, String name, String domainOrNull) {
		ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(name, "")
			.path("/")
			.httpOnly(true)
			.secure(true)
			.sameSite("Lax")
			.maxAge(Duration.ZERO);

		if (domainOrNull != null)
			b.domain(domainOrNull);
		var built = b.build();
		var header = built.toString();
		log.debug("[COOKIE][DEL] name={} domain={} -> {}", name, domainOrNull, header);
		response.addHeader(HttpHeaders.SET_COOKIE, header);
	}

	public static void deleteCookie(HttpServletResponse response, String name) {
		// host-only
		addDeletion(response, name, null);
		// 서브도메인
		addDeletion(response, name, "api.grabpt.com");
		// 최상위 도메인
		addDeletion(response, name, "grabpt.com");
	}

	// ----- 직렬화 유틸 -----
	public static String serialize(Object object) {
		return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(object));
	}

	public static <T> T deserialize(Cookie cookie, Class<T> cls) {
		return cls.cast(SerializationUtils.deserialize(Base64.getUrlDecoder().decode(cookie.getValue())));
	}
}
