package com.grabpt.service.RefundService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.grabpt.dto.request.RefundRequest;
import com.grabpt.dto.response.RefundResponse;

public interface RefundService {

    RefundResponse.RefundResponseDto requestRefund(Long userId, RefundRequest.RefundRequestDto request);

    RefundResponse.RefundResponseDto approveRefund(Long proUserId, Long refundId);

    RefundResponse.RefundResponseDto rejectRefund(Long proUserId, Long refundId, RefundRequest.RefundRejectDto request);

    RefundResponse.RefundResponseDto getRefund(Long userId, Long refundId);

    Page<RefundResponse.RefundResponseDto> getMyRefunds(Long userId, Pageable pageable);
}
