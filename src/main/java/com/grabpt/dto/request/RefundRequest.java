package com.grabpt.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

public class RefundRequest {

    @Getter
    @NoArgsConstructor
    public static class RefundRequestDto {
        private Long orderId;
        private String reason;
    }

    @Getter
    @NoArgsConstructor
    public static class RefundRejectDto {
        private String rejectionReason;
    }
}
