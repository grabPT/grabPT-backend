package com.grabpt.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OAuthTempInfoDto {

	@JsonProperty("oauthProvider")
	private String oauthProvider;   // 예: "google" | "kakao" | "naver"

	@JsonProperty("oauthId")
	private String oauthId;         // 예: "google-1234567890"

	@JsonProperty("username")
	private String username;        // 예: 소셜 프로필 이름/닉네임

	@JsonProperty("email")
	private String email;           // 예: "user@example.com"
}
