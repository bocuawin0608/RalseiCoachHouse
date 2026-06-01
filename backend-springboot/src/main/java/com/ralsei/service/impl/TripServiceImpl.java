package com.ralsei.service.impl;

import com.ralsei.dto.projection.TripDetailProjection;
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
            int size
    ) {
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
                tripPage.isLast()
        );
    }
}