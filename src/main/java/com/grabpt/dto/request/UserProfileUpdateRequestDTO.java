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

	@Valid
	private AddressDTO address;

	@Getter
	public static class AddressDTO {
		@NotBlank(message = "시는 필수 입력 항목입니다.")
		private String city;

		@NotBlank(message = "구/군은 필수 입력 항목입니다.")
		private String district;

		private String street;

		private String specAddress;

		private String zipcode;
	}
}
