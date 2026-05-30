package com.ralsei.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ralsei.dto.request.seatlayout.SeatLayoutCreateRequest;
import com.ralsei.dto.request.seatlayout.SeatLayoutFilterRequest;
import com.ralsei.dto.request.seatlayout.SeatLayoutUpdateInfoRequest;
import com.ralsei.dto.request.seatlayout.SeatLayoutUpdatePriceRequest;
import com.ralsei.dto.request.seatlayout.SeatLayoutUpdateSeatRequest;
import com.ralsei.dto.response.seatlayout.SeatLayoutDetailResponse;
import com.ralsei.dto.response.seatlayout.SeatLayoutResponse;

public interface SeatLayoutService {
    Page<SeatLayoutResponse> filterSeatLayouts(SeatLayoutFilterRequest filterRequest, Pageable pageable);
    SeatLayoutResponse createSeatLayout(SeatLayoutCreateRequest request);
    SeatLayoutDetailResponse getSeatLayoutDetail(Integer seatLayoutId);
    SeatLayoutDetailResponse updateSeatLayoutInfo(Integer seatLayoutId, SeatLayoutUpdateInfoRequest request);
    SeatLayoutDetailResponse updateSeatLayoutPrice(Integer seatLayoutId, SeatLayoutUpdatePriceRequest request);
    SeatLayoutDetailResponse updateSeatLayoutSeats(Integer seatLayoutId, SeatLayoutUpdateSeatRequest request);
}
