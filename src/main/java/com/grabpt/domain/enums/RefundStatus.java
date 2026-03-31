package com.grabpt.domain.enums;

public enum RefundStatus {
    PENDING,        // 환불 신청됨 (PRO 확인 대기)
    APPROVED,       // PRO가 승인 (PortOne 환불 처리 완료)
    REJECTED,       // PRO가 거절
    AUTO_APPROVED   // 시작일 전 자동 전액 환불
}
