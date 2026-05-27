package com.ralsei.service;

import com.ralsei.dto.projection.TripDetailProjection;
import com.ralsei.dto.response.PagedResponse;
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
