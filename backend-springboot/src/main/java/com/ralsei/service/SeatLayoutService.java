package com.ralsei.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ralsei.dto.request.seatlayout.SeatLayoutCreateRequest;
import com.ralsei.dto.request.seatlayout.SeatLayoutFilterRequest;
import com.ralsei.dto.response.seatlayout.SeatLayoutDetailResponse;
import com.ralsei.dto.response.seatlayout.SeatLayoutResponse;

public interface SeatLayoutService {
    Page<SeatLayoutResponse> filterSeatLayouts(SeatLayoutFilterRequest filterRequest, Pageable pageable);
    SeatLayoutResponse createSeatLayout(SeatLayoutCreateRequest request);
    SeatLayoutDetailResponse getSeatLayoutDetail(Integer seatLayoutId);
}
