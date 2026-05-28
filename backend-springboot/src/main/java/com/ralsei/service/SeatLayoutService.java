package com.ralsei.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ralsei.dto.projection.SeatLayoutProjection;
import com.ralsei.dto.request.seatlayout.SeatLayoutFilterRequest;

public interface SeatLayoutService {
    Page<SeatLayoutProjection> filterSeatLayouts(SeatLayoutFilterRequest filterRequest, Pageable pageable);

    
}
