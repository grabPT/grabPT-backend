package com.grabpt.dto.response;

import java.util.List;

import com.grabpt.domain.entity.Requestions;

import lombok.Getter;
@Getter
public class MyRequestListDTO {
	private Long requestId;
	private String imageURL;
	private Long userId;
	private String location;
	private List<String> availableTimes;
	private String categoryName;
	private Integer sessionCount;
	private String content;



	public MyRequestListDTO(Requestions requestion) {
		this.requestId = requestion.getId();
		this.imageURL = requestion.getUser().getProfileImageUrl();
		this.userId = requestion.getUser().getId();
		this.location = requestion.getLocation();
		this.availableTimes = requestion.getAvailableTimes();
		this.categoryName = requestion.getCategory().getName();
		this.sessionCount = requestion.getSessionCount();
		this.content = requestion.getContent();

	}

}
