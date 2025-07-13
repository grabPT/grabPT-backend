package com.grabpt.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;

@Getter
public class UserProfileUpdateRequestDTO {

	@NotBlank(message = "닉네임은 필수 입력 항목입니다.")
	@Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 입력해주세요.")
	private String nickname;

	private String residence;

	@Size(max = 3, message = "운동 희망 지역은 최대 3개까지 선택 가능합니다.")
	private List<String> preferredAreas;
}
