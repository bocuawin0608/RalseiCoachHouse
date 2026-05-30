package com.ralsei.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ralsei.model.Seat;

public interface SeatRepository extends JpaRepository<Seat, Integer> {
    
}
