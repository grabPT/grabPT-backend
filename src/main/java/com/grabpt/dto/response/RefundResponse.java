package com.grabpt.dto.response;

import java.time.LocalDateTime;

import com.grabpt.domain.enums.RefundStatus;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

public class RefundResponse {

    @Getter
    @Builder
    public static class RefundResponseDto {
        private Long refundId;
        private Long orderId;
        private Long userId;
        private RefundStatus status;
        private String reason;
        private Long refundAmount;
        private String refundUid;
        private String rejectionReason;
        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    public static class RefundListResponseDto {
        private Page<RefundResponseDto> refunds;
    }
}
