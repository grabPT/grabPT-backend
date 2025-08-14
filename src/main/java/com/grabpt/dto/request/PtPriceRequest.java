package com.grabpt.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class PtPriceRequest {
	@Getter
	public static class PtPriceUpdateRequestDto{
		private int pricePerSession;
		private int totalSessions;
	}

	@Getter
	public static class PtPriceUpdateRequestList{
		List<PtPriceUpdateRequestDto> ptPriceUpdateRequestDtoList;
	}
}
