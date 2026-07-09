package com.ralsei.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ralsei.model.AccompaniedChild;

public interface AccompaniedChildRepository extends JpaRepository<AccompaniedChild, Integer> {
    Optional<AccompaniedChild> findByTicketDetailId(Integer ticketDetailId);
}
