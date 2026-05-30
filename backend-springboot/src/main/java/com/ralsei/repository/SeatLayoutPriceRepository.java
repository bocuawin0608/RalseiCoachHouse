package com.ralsei.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ralsei.model.SeatLayoutPrice;
public interface SeatLayoutPriceRepository extends JpaRepository<SeatLayoutPrice, Integer> {
    
}
