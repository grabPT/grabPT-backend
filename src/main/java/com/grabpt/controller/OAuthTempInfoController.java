package com.grabpt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.dto.response.OAuthTempInfoDto;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class OAuthTempInfoController {

	@GetMapping("/temp-info")
	public ResponseEntity<ApiResponse<OAuthTempInfoDto>> temp(HttpSession session) {

		String email = (String)session.getAttribute("tempEmail");
		String username = (String)session.getAttribute("tempName");
		String oauthProvider = (String)session.getAttribute("tempOauthProvider");
		Object oauthIdObj = session.getAttribute("tempOauthId");
		String oauthId = oauthIdObj == null ? null : String.valueOf(oauthIdObj);

		if (email == null && username == null && oauthProvider == null && oauthId == null) {
			return ResponseEntity.noContent().build();
		}

		OAuthTempInfoDto body = OAuthTempInfoDto.builder()
			.oauthProvider(oauthProvider)
			.oauthId(oauthId)
			.username(username)
			.email(email)
			.build();

		return ResponseEntity.ok(ApiResponse.onSuccess(body));
	}
}
