package com.tuanvm.service;

import com.tuanvm.dto.projection.TripDetailProjection;
import com.tuanvm.dto.response.PagedResponse;
import java.time.LocalDateTime;

public interface TripService {
    PagedResponse<TripDetailProjection> getTripDetails(
            LocalDateTime start,
            LocalDateTime end,
            String route,
            int page,
            int size
    );
}
