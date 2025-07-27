package com.grabpt.service.PaymentService;

import com.grabpt.dto.request.ImPortRequestDto;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;

public interface PaymentService {
	// 결제 요청 데이터 조회
	ImPortRequestDto.RequestPayDto findRequestDto(String orderUid);

	// 결제 요청 데이터 조회 커스텀
	ImPortRequestDto.CustomRequestPayDto findCustomRequestDto(String orderUid);

	// 결제(콜백)
	IamportResponse<Payment> paymentByCallback(ImPortRequestDto.PaymentCallbackRequest request);
}
