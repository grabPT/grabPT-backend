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
		private String order_uid;
		private String item_name;
		private String buyer_name;
		private Long payment_price;
		private String buyer_email;
		private String buyer_address;
		private String buyer_tel;
		private String buyer_postcode;

		@Builder
		public CustomRequestPayDto(String orderUid, String itemName, String buyerName,
			Long paymentPrice, String buyerEmail, String buyerAddress, String buyerTel,
			String buyerPostCode) {
			this.order_uid = orderUid;
			this.item_name = itemName;
			this.buyer_name = buyerName;
			this.payment_price = paymentPrice;
			this.buyer_email = buyerEmail;
			this.buyer_address = buyerAddress;
			this.buyer_tel = buyerTel;
			this.buyer_postcode = buyerPostCode;
		}
	}

	@Getter
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	@NoArgsConstructor
	public static class CustomOrderRequestDto {
		private Long price; // 결제 고유 번호
		private String item_name; // 주문 고유 번호
		private Long matching_id;
	}

	@Getter
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	@NoArgsConstructor
	public static class PaymentCallbackRequest {
		private String payment_uid; // 결제 고유 번호
		private String order_uid; // 주문 고유 번호

	}
}
