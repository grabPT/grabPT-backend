package com.grabpt.dto.response;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.grabpt.domain.entity.Address;
import com.grabpt.domain.entity.Requestions;

import lombok.Builder;
import lombok.Getter;
@Getter
public class MyRequestListDTO {
	private Long requestId;
	private String imageURL;
	private Long userId;
	private List<String> availableDays;
	private List<String> availableTimes;
	private String categoryName;
	private Integer sessionCount;
	private String content;
	private AddressDTO address;

	@Getter
	@Builder
	public static class AddressDTO {
		private String city;
		private String district;
		private String street;
		private String zipcode;

		public static AddressDTO from(Address address) {
			return AddressDTO.builder()
				.city(address.getCity())
				.district(address.getDistrict())
				.street(address.getStreet())
				.zipcode(address.getZipcode())
				.build();
		}
	}

	public MyRequestListDTO(Requestions requestion) {
		this.requestId = requestion.getId();
		this.imageURL = requestion.getUser().getProfileImageUrl();
		this.userId = requestion.getUser().getId();

		// 이 부분을 수정했습니다.
		Address addresses = requestion.getUser().getAddress();
		this.address = AddressDTO.from(addresses);
		this.availableDays = requestion.getAvailableDays();
		this.availableTimes = requestion.getAvailableTimes();
		this.categoryName = requestion.getCategory().getName();
		this.sessionCount = requestion.getSessionCount();
		this.content = requestion.getContent();
	}

}
