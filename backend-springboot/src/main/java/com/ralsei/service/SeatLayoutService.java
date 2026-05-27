package com.tuanvm.service;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;

import com.tuanvm.dto.projection.SeatLayoutProjection;

public interface SeatLayoutService {
    Page<SeatLayoutProjection> filterSeatLayouts(
        String seatLayoutName,
        Boolean isActive,
        BigDecimal minPrice, BigDecimal maxPrice,
        Integer minSeats, Integer maxSeats,
        int page, int size
    );

    
}
