package com.ralsei.service;

import com.ralsei.dto.projection.trip.TripDetailProjection;
import com.ralsei.dto.projection.trip.TripFilterProjection;
import com.ralsei.dto.projection.trip.TripSummaryProjection;
import com.ralsei.dto.response.PagedResponse;
import java.time.LocalDateTime;
import java.util.List;

public interface TripService {
    PagedResponse<TripDetailProjection> getTripDetails(
            LocalDateTime start,
            LocalDateTime end,
            String route,
            int page,
            int size

    );
    PagedResponse<TripFilterProjection> getFilteredTripDetails(
            LocalDateTime start,
            LocalDateTime end,
            String route,
            List<String> timeSlots,
            List<String> layouts,
            Double minPrice,
            Double maxPrice,
            int page,
            int size
    );
    PagedResponse<TripSummaryProjection> getAllTripSummaries(int page, int size);
    // PagedResponse<TripDetailProjection> getFilteredTripDetails(
    //     LocalDateTime start,
    //     LocalDateTime end,
    //     String route,
    //     List<String> timeSlots,
    //     List<String> layouts,
    //     Double minPrice,
    //     Double maxPrice,
    //     int page,
    //     int size
    // );
}
