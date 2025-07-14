package com.grabpt.dto.response;

import java.time.LocalDateTime;

import com.grabpt.domain.entity.Requestions;
import com.grabpt.domain.enums.RequestStatus;

import lombok.Getter;
@Getter
public class MyRequestListDTO {
	private Long requestId;
	private String cateogryName;
	private Integer sessionCount;
	private Integer price;
	private RequestStatus status;
	private String location;
	private LocalDateTime createdAt;


	public MyRequestListDTO(Requestions requestion) {
		this.requestId = requestion.getId();
		this.cateogryName = requestion.getCategory().getName();
		this.sessionCount = requestion.getSessionCount();
		this.price = requestion.getPrice();
		this.status = requestion.getStatus();
		this.location = requestion.getLocation();
		this.createdAt = requestion.getCreatedAt();
	}

}
