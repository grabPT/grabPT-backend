package com.grabpt.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.config.jwt.properties.CookieManager;
import com.grabpt.config.jwt.properties.CookieManager.OAuthTempInfo;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthControllerOAuthHelper {

	/**
	 * OAuth 회원가입용 임시 정보 조회
	 * - HttpOnly 쿠키에서 서버 사이드로만 조회 가능
	 * - XSS 공격으로부터 안전
	 */
	@Operation(
		summary = "OAuth 임시 정보 조회",
		description = "소셜 로그인 후 회원가입에 필요한 임시 정보를 조회합니다. 정보는 HttpOnly 쿠키에 저장되어 있어 서버에서만 접근 가능합니다."
	)
	@GetMapping("/oauth/temp-info")
	public ApiResponse<Map<String, String>> getOAuthTempInfo(HttpServletRequest request) {

		// HttpOnly 쿠키에서 OAuth 정보 조회
		OAuthTempInfo tempInfo = CookieManager.getOAuthTempInfo(request);

		if (!tempInfo.isValid()) {
			log.warn("[OAuth TempInfo] Invalid or expired temporary OAuth information");
			return ApiResponse.onFailure("400", "OAuth 임시 정보가 없거나 만료되었습니다", null);
		}

		Map<String, String> data = new HashMap<>();
		data.put("email", tempInfo.email());
		data.put("username", tempInfo.name());
		data.put("oauthProvider", tempInfo.provider());
		data.put("oauthId", tempInfo.oauthId());

		log.info("[OAuth TempInfo] Retrieved for provider: {}", tempInfo.provider());

		return ApiResponse.onSuccess(data);
	}
}

