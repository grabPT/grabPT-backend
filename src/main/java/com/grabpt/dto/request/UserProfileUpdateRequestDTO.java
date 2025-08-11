package com.grabpt.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;

@Getter
public class UserProfileUpdateRequestDTO {

	@NotBlank(message = "닉네임은 필수 입력 항목입니다.")
	@Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 입력해주세요.")
	private String nickname;

	/**
	 * 수정할 주소 목록입니다. (거주지 및 선호 지역)
	 * 최대 3개까지 입력 가능합니다.
	 */
	@Valid
	private AddressDTO address;

	/**
	 * 주소 정보를 담는 내부 DTO 클래스
	 */
	@Getter
	public static class AddressDTO {
		@NotBlank(message = "시는 필수 입력 항목입니다.")
		private String city;

		@NotBlank(message = "구/군은 필수 입력 항목입니다.")
		private String district;

		private String street;

		private String zipcode;
	}
}
