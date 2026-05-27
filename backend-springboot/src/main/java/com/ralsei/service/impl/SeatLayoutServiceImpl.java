package com.tuanvm.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tuanvm.dto.projection.SeatLayoutProjection;
import com.tuanvm.repository.SeatLayoutRepository;
import com.tuanvm.service.SeatLayoutService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatLayoutServiceImpl implements SeatLayoutService {
    
    private final SeatLayoutRepository seatLayoutRepository;

    @Override
    public Page<SeatLayoutProjection> filterSeatLayouts(
        String seatLayoutName,
        Boolean isActive,
        BigDecimal minPrice, BigDecimal maxPrice,
        Integer minSeats, Integer maxSeats,
        int page, int size
    ) {
        return seatLayoutRepository.searchSeatLayouts(seatLayoutName, isActive, minPrice, maxPrice, minSeats, maxSeats, LocalDateTime.now(), PageRequest.of(page, size, Sort.by("seatLayoutId").descending()));
    }
}
