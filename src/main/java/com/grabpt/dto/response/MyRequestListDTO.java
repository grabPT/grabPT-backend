package com.grabpt.dto.response;

import java.util.List;

import com.grabpt.domain.entity.Requestions;

import lombok.Getter;
@Getter
public class MyRequestListDTO {
	private Long requestId;
	private String imageURL;
	private Long userId;
	private String city;
	private String district;
	private String street;
	private List<String> availableTimes;
	private String categoryName;
	private Integer sessionCount;
	private String content;



	public MyRequestListDTO(Requestions requestion) {
		this.requestId = requestion.getId();
		this.imageURL = requestion.getUser().getProfileImageUrl();
		this.userId = requestion.getUser().getId();
		this.city = requestion.getUser().getAddress().getCity();
		this.district = requestion.getUser().getAddress().getDistrict();
		this.street = requestion.getUser().getAddress().getStreet();
		this.availableTimes = requestion.getAvailableTimes();
		this.categoryName = requestion.getCategory().getName();
		this.sessionCount = requestion.getSessionCount();
		this.content = requestion.getContent();

	}

}
