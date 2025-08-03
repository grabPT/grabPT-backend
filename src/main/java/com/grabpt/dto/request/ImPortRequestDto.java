package com.grabpt.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Builder
@Getter
@NoArgsConstructor
public class ImPortRequestDto {

	@Builder
	@Getter
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	@NoArgsConstructor
	public static class RequestPayDto {
		private String orderUid;
		private String itemName;
		private String buyerName;
		private Long paymentPrice;
		private String buyerEmail;
		private String buyerAddress;

		@Builder
		public RequestPayDto(String orderUid, String itemName, String buyerName, Long paymentPrice, String buyerEmail,
			String buyerAddress) {
			this.orderUid = orderUid;
			this.itemName = itemName;
			this.buyerName = buyerName;
			this.paymentPrice = paymentPrice;
			this.buyerEmail = buyerEmail;
			this.buyerAddress = buyerAddress;
		}
	}

	@Builder
	@Getter
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	@NoArgsConstructor
	public static class CustomRequestPayDto {
		private String orderUid;
		private String itemName;
		private String buyerName;
		private Long paymentPrice;
		private String buyerEmail;
		private String buyerAddress;
		private String buyerTel;
		private String buyerPostcode;

		@Builder
		public CustomRequestPayDto(String orderUid, String itemName, String buyerName,
			Long paymentPrice, String buyerEmail, String buyerAddress, String buyerTel,
			String buyerPostCode) {
			this.orderUid = orderUid;
			this.itemName = itemName;
			this.buyerName = buyerName;
			this.paymentPrice = paymentPrice;
			this.buyerEmail = buyerEmail;
			this.buyerAddress = buyerAddress;
			this.buyerTel = buyerTel;
			this.buyerPostcode = buyerPostCode;
		}
	}

	@Getter
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	@NoArgsConstructor
	public static class CustomOrderRequestDto {
		private Long price; // 결제 고유 번호
		private String itemName; // 주문 고유 번호
		private Long matchingId;
	}

	@Getter
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	@NoArgsConstructor
	public static class PaymentCallbackRequest {
		private String paymentUid; // 결제 고유 번호
		private String orderUid; // 주문 고유 번호
	}
}
