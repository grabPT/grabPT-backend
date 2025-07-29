package com.grabpt.repository.PaymentRepository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grabpt.domain.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
