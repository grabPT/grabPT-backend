package com.grabpt.domain.entity;

import com.grabpt.domain.common.BaseEntity;
import com.grabpt.domain.enums.RefundStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refund extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @Enumerated(EnumType.STRING)
    private RefundStatus status;

    private String reason;           // 환불 신청 사유
    private Long refundAmount;       // 환불 금액
    private String refundUid;        // PortOne 환불 트랜잭션 ID
    private String rejectionReason;  // 거절 사유

    @Builder
    public Refund(Order order, Users user, RefundStatus status, String reason, Long refundAmount) {
        this.order = order;
        this.user = user;
        this.status = status;
        this.reason = reason;
        this.refundAmount = refundAmount;
    }

    public void approve(String refundUid) {
        this.status = RefundStatus.APPROVED;
        this.refundUid = refundUid;
    }

    public void autoApprove(String refundUid) {
        this.status = RefundStatus.AUTO_APPROVED;
        this.refundUid = refundUid;
    }

    public void reject(String rejectionReason) {
        this.status = RefundStatus.REJECTED;
        this.rejectionReason = rejectionReason;
    }
}
