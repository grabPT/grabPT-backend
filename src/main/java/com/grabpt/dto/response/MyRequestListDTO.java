package com.grabpt.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.grabpt.domain.entity.Address;
import com.grabpt.domain.entity.Requestions;
import com.grabpt.domain.enums.RequestStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyRequestListDTO {
	private Long requestionId;
	private String profileImageURL;
	private Long userId;
	private List<String> availableDays;
	private List<String> availableTimes;
	private String categoryName;
	private Integer sessionCount;
	private String content;
	private AddressDTO address;
	private RequestStatus matchingStatus;

	// 추가
	private String proNickname;
	private Long proId;
	@JsonProperty("isWriteReview")
	private boolean isWriteReview;

	public void setProProfileId(Long proId) {
		this.proId = proId;
	}

	public void setCanWriteReview(boolean canWriteReview) {
		this.isWriteReview = canWriteReview;
	}

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
		this.requestionId = requestion.getId();
		this.profileImageURL = requestion.getUser().getProfileImageUrl();
		this.userId = requestion.getUser().getId();
		Address addresses = requestion.getUser().getAddress();
		this.address = AddressDTO.from(addresses);
		this.availableDays = requestion.getAvailableDays();
		this.availableTimes = requestion.getAvailableTimes();
		this.categoryName = requestion.getCategory().getName();
		this.sessionCount = requestion.getSessionCount();
		this.content = requestion.getContent();
		this.matchingStatus = requestion.getStatus();
	}
}
