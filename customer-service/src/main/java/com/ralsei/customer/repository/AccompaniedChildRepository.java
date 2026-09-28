package com.ralsei.customer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ralsei.customer.model.AccompaniedChild;

/**
 * Provides persistence access for accompanied child data.
 */
public interface AccompaniedChildRepository extends JpaRepository<AccompaniedChild, Integer> {
    Optional<AccompaniedChild> findByTicketDetailId(Integer ticketDetailId);
}
