package com.grabpt.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.dto.response.OAuthTempInfoDto;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class OAuthTempInfoController {

	@GetMapping("/temp-info")
	public ApiResponse<OAuthTempInfoDto> temp(HttpSession session) {

		String email = (String)session.getAttribute("tempEmail");
		String username = (String)session.getAttribute("tempName");
		String oauthProvider = (String)session.getAttribute("tempOauthProvider");
		Object oauthIdObj = session.getAttribute("tempOauthId");
		String oauthId = oauthIdObj == null ? null : String.valueOf(oauthIdObj);

		OAuthTempInfoDto body = OAuthTempInfoDto.builder()
			.oauthProvider(oauthProvider)
			.oauthId(oauthId)
			.username(username)
			.email(email)
			.build();

		return ApiResponse.onSuccess(body);
	}
}
