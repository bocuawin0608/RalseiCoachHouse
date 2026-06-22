package com.ralsei.service;

import com.ralsei.dto.projection.coach.CoachLicensePlateProjection;
import com.ralsei.dto.projection.staff.StaffProjection;
import com.ralsei.dto.projection.trip.TripDetailProjection;
import com.ralsei.dto.projection.trip.TripFilterProjection;
import com.ralsei.dto.projection.trip.TripSummaryProjection;
import com.ralsei.dto.request.trip.TripCreateRequest;
import com.ralsei.dto.request.trip.TripUpdateRequest;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.dto.response.CoachAndRouteStop.RouteDropdownDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TripService {
        /**
         * This interface class use to get all trip in the day customer site
         * 
         * @param start time and date
         * @param end   time and date
         * @param route from start to end from input
         * @param page  to split or user eye will fucked with the 1km long data
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
                        int size);

        PagedResponse<TripSummaryProjection> getAllTripSummaries(LocalDate date, int page, int size);

        // PagedResponse<TripDetailProjection> getFilteredTripDetails(
        // LocalDateTime start,
        // LocalDateTime end,
        // String route,
        // List<String> timeSlots,
        // List<String> layouts,
        // Double minPrice,
        // Double maxPrice,
        // int page,
        // int size
        // );
        public String insertTrip(TripCreateRequest tripRequest);

        public String updateTrip(Integer tripId, TripUpdateRequest updateRequest);

        public String deleteTrip(Integer tripId);

        public List<RouteDropdownDTO> findRoutesForDropdown();

        List<StaffProjection> getStaffNameDropDown(LocalDate date);

        List<CoachLicensePlateProjection> getCoachInfoDropDown(LocalDate date);

}
