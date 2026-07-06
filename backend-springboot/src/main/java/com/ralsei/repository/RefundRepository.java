package com.ralsei.repository;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ralsei.model.Refund;

/** Persistence boundary for customer and staff refund workflows. */
public interface RefundRepository extends JpaRepository<Refund, Integer> {

    /** Prevents duplicate active refunds for the same completed payment. */
    boolean existsByPaymentIdAndStatusIn(Integer paymentId, Collection<String> statuses);
}
