package com.grabpt.config.jwt.properties;

public final class CookieConstants {

	// JWT 토큰 쿠키
	public static final String ACCESS_TOKEN = "accessToken";
	public static final String REFRESH_TOKEN = "refreshToken";

	// 사용자 정보 쿠키
	public static final String ROLE = "role";
	public static final String USER_ID = "userId";

	// OAuth 임시 정보 쿠키 (회원가입용)
	public static final String OAUTH_EMAIL = "oauthEmail";
	public static final String OAUTH_NAME = "oauthName";
	public static final String OAUTH_ID = "oauthId";
	public static final String OAUTH_PROVIDER = "oauthProvider";

	// OAuth 플로우 쿠키
	public static final String REDIRECT_URI = "oauth2_redirect_uri";
	public static final String AUTHORIZATION_REQUEST = "oauth2_auth_request";

	private CookieConstants() {
		throw new AssertionError("Cannot instantiate constants class");
	}
}
