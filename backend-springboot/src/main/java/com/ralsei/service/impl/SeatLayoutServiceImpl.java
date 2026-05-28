package com.ralsei.service.impl;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.dto.projection.SeatLayoutProjection;
import com.ralsei.dto.request.seatlayout.SeatLayoutFilterRequest;
import com.ralsei.repository.SeatLayoutRepository;
import com.ralsei.service.SeatLayoutService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatLayoutServiceImpl implements SeatLayoutService {
    
    private final SeatLayoutRepository seatLayoutRepository;

    @Transactional(readOnly = true)
    @Override
    public Page<SeatLayoutProjection> filterSeatLayouts(SeatLayoutFilterRequest filterRequest, Pageable pageable) {
        
        if(filterRequest.minPrice() != null && filterRequest.maxPrice() != null && filterRequest.minPrice().compareTo(filterRequest.maxPrice()) > 0) {
            throw new IllegalArgumentException("Giá tối thiểu không thể lớn hơn Giá tối đa!");
        }
        
        if(filterRequest.minSeats() != null && filterRequest.maxSeats() != null && filterRequest.minSeats() > filterRequest.maxSeats()) {
            throw new IllegalArgumentException("Giá tối thiểu không thể lớn hơn Giá tối đa!");
        }
        
        return seatLayoutRepository.searchSeatLayouts(filterRequest, LocalDateTime.now(), pageable);
    }
}
