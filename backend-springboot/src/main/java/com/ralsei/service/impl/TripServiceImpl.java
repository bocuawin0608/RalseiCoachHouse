package com.ralsei.service.impl;

import com.ralsei.dto.projection.trip.TripDetailProjection;
import com.ralsei.dto.projection.trip.TripFilterProjection;
import com.ralsei.dto.projection.trip.TripSummaryProjection;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.repository.TripRepository;
import com.ralsei.service.TripService;
import com.ralsei.util.FormatHandlerUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TripDetailProjection> getTripDetails(
            LocalDateTime start,
            LocalDateTime end,
            String route,
            int page,
            int size) {
        route = FormatHandlerUtility.formatProvinceName(route);
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("Your mom are in the past, please check your date range!");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<TripDetailProjection> tripPage = tripRepository.findTripDetails(start, end, route, pageable);

        return new PagedResponse<>(
                tripPage.getContent(),
                tripPage.getNumber(),
                tripPage.getSize(),
                tripPage.getTotalElements(),
                tripPage.getTotalPages(),
                tripPage.isLast());
    }

       @Override
    @Transactional(readOnly = true)
    public PagedResponse<TripFilterProjection> getFilteredTripDetails(
            LocalDateTime start,
            LocalDateTime end,
            String route,
            List<String> timeSlots,
            List<String> layouts,
            Double minPrice,
            Double maxPrice,
            int page,
            int size) {
 
        // 1. Phân rã bộ lọc giờ đi (tối đa 4 slot)
        int checkTimeSlots = (timeSlots != null && !timeSlots.isEmpty()) ? 1 : 0;
 
        String slot1Start = null, slot1End = null;
        String slot2Start = null, slot2End = null;
        String slot3Start = null, slot3End = null;
        String slot4Start = null, slot4End = null;
 
        if (checkTimeSlots == 1) {
            for (int i = 0; i < Math.min(timeSlots.size(), 4); i++) {
                String slot = timeSlots.get(i);
                if (slot != null && slot.contains("-")) {
                    String[] parts = slot.split("-");
                    String sTime = parts[0].trim();
                    String eTime = parts[1].trim();
                    if (sTime.length() == 5) sTime += ":00";
                    if (eTime.length() == 5) eTime += ":00";
 
                    switch (i) {
                        case 0 -> { slot1Start = sTime; slot1End = eTime; }
                        case 1 -> { slot2Start = sTime; slot2End = eTime; }
                        case 2 -> { slot3Start = sTime; slot3End = eTime; }
                        case 3 -> { slot4Start = sTime; slot4End = eTime; }
                    }
                }
            }
        }
 
        // 2. Phân rã bộ lọc loại xe (LIKE clause)
        int checkLayouts = (layouts != null && !layouts.isEmpty()) ? 1 : 0;
        String layoutKeyword1 = null;
        String layoutKeyword2 = null;
 
        if (checkLayouts == 1) {
            if (layouts.size() > 0) layoutKeyword1 = "%" + layouts.get(0).trim() + "%";
            if (layouts.size() > 1) layoutKeyword2 = "%" + layouts.get(1).trim() + "%";
        }
 
        // 3. Khởi tạo phân trang
        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime now = LocalDateTime.now();
 
        // 4. Gọi repository
        Page<TripFilterProjection> filteredPage = tripRepository.filterTrips(
                now, start, end, route,
                checkTimeSlots,
                slot1Start, slot1End,
                slot2Start, slot2End,
                slot3Start, slot3End,
                slot4Start, slot4End,
                checkLayouts,
                layoutKeyword1, layoutKeyword2,
                minPrice, maxPrice,
                pageable
        );
 
        // 5. Đóng gói response
        return new PagedResponse<>(
                filteredPage.getContent(),
                filteredPage.getNumber(),
                filteredPage.getSize(),
                filteredPage.getTotalElements(),
                filteredPage.getTotalPages(),
                filteredPage.isLast()
        );
    }

       @Override
       public PagedResponse<TripSummaryProjection> getAllTripSummaries(int page, int size) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllTripSummaries'");
       }

    }
