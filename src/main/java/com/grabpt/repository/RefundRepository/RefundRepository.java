package com.grabpt.repository.RefundRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grabpt.domain.entity.Refund;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    boolean existsByOrderId(Long orderId);

    Optional<Refund> findByOrderId(Long orderId);

    @Query("SELECT r FROM Refund r WHERE r.user.id = :userId ORDER BY r.id DESC")
    Page<Refund> findAllByUserId(@Param("userId") Long userId, Pageable pageable);
}
