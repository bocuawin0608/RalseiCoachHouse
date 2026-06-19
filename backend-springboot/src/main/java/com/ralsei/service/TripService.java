package com.ralsei.service;

import com.ralsei.dto.projection.trip.TripDetailProjection;
import com.ralsei.dto.projection.trip.TripFilterProjection;
import com.ralsei.dto.projection.trip.TripSummaryProjection;
import com.ralsei.dto.request.trip.TripUpdateRequest;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.model.Trip;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public interface TripService {
        /**
         * This interface class use to get all trip in the day customer site
         * @param start time and date
         * @param end time and date
         * @param route from start to end from input
         * @param page to split or user eye will fucked with the 1km long data
         * @param size
         * @return
         */
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
    PagedResponse<TripSummaryProjection> getAllTripSummaries(LocalDate date, int page, int size);
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
    public String createTrip(Trip trip);
    public String updateTrip(Integer tripId, TripUpdateRequest updateRequest);
    public String deleteTrip(Integer tripId);
    
}
