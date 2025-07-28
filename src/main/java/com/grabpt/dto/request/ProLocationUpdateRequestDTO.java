package com.grabpt.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ProLocationUpdateRequestDTO {

	@NotBlank(message = "센터명은 필수입니다.")
	private String center;
	private String centerDescription;

	@NotBlank(message = "대표 주소(시/도)는 필수입니다.")
	private String city;

	@NotBlank(message = "대표 주소(시/군/구)는 필수입니다.")
	private String district;

	@NotBlank(message = "대표 주소(읍/면/동)는 필수입니다.")
	private String street;

	@NotBlank(message = "우편번호는 필수입니다.")
	private String zipcode;
}
