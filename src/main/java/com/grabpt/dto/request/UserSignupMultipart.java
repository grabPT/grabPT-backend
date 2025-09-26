package com.grabpt.dto.request;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(name = "UserSignupMultipart")
@Getter
public class UserSignupMultipart {
	@Schema(description = "회원가입 JSON")
	private SignupRequest.UserSignupRequestDto data;

	@Schema(type = "string", format = "binary", description = "프로필 이미지 파일(선택)")
	private MultipartFile profileImage;

	// getter/setter 필요 (롬복 사용 가능)
}
