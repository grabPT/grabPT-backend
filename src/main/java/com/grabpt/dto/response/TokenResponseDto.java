package com.grabpt.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenResponseDto {
	@Schema(description = "Access 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
	private String accessToken;
	@Schema(description = "Refresh 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCASDFfdvJ9...")
	private String refreshToken;
	@Schema(description = "토큰 기반 인증 타입", example = "Bearer")
	private String grantType;
	@Schema(description = "만료 기간", example = "86400")
	private long expiresIn;
}
