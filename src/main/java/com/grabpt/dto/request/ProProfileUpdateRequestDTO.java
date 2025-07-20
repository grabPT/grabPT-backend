package com.grabpt.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ProProfileUpdateRequestDTO {

	@NotBlank(message = "닉네임은 필수 입력 항목입니다.")
	@Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 입력해주세요.")
	private String nickname;

	private String residence;

	private String center;

	private Integer career;

	@Size(max = 3, message = "운동 희망 지역은 최대 3개까지 선택 가능합니다.")
	private List<String> preferredAreas;

	@Size(max = 2000)
	private String description; // 전문가 소개

	@Valid
	private List<PhotoRequestDTO> photos; // 소개 사진

	private String programDescription;
	private Integer pricePerSession;
	private Integer totalSessions;

	@Valid
	private List<CertificationRequestDTO> certifications;

	@Size(max = 3)
	private List<String> activityAreas;

}
