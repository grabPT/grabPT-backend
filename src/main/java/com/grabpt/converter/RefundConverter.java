package com.grabpt.converter;

import com.grabpt.domain.entity.Refund;
import com.grabpt.dto.response.RefundResponse;

public class RefundConverter {

    public static RefundResponse.RefundResponseDto toRefundResponseDto(Refund refund) {
        return RefundResponse.RefundResponseDto.builder()
            .refundId(refund.getId())
            .orderId(refund.getOrder().getId())
            .userId(refund.getUser().getId())
            .status(refund.getStatus())
            .reason(refund.getReason())
            .refundAmount(refund.getRefundAmount())
            .refundUid(refund.getRefundUid())
            .rejectionReason(refund.getRejectionReason())
            .createdAt(refund.getCreatedAt())
            .build();
    }
}
