package com.ralsei.service;

import com.ralsei.dto.projection.coach.CoachLicensePlateProjection;
import com.ralsei.dto.projection.staff.StaffProjection;
import com.ralsei.dto.projection.trip.StaffTripInfoProjection;
import com.ralsei.dto.projection.trip.TripDetailProjection;
import com.ralsei.dto.projection.trip.TripFilterProjection;
import com.ralsei.dto.projection.trip.TripSummaryProjection;
import com.ralsei.dto.projection.trip.TripStopProjection;
import com.ralsei.dto.projection.trip.TripResourceProjection;
import com.ralsei.dto.request.trip.TripCreateRequest;
import com.ralsei.dto.request.trip.TripUpdateRequest;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.dto.response.CoachAndRouteStop.RouteDropdownDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Application boundary for customer search and staff trip-management use cases.
 * Customer methods return public projections; staff methods retain their
 * operational projections and are intentionally unaffected by customer rules.
 */
/**
 * Provides the business service contract for trip.
 */
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

        /**
         * Searches selectable customer trips using optional time, coach, and
         * price filters that have already passed transport validation.
         */
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

        /** Returns staff trip summaries using user-facing date, route and period filters. */
        PagedResponse<TripSummaryProjection> getAllTripSummaries(LocalDate date, Integer routeId, String period, int page, int size);

        /**
         * Returns upcoming trip information for ticket staff.
         *
         * @param date selected departure date; defaults to today when omitted
         * @param city departure city parsed from route name, optional
         * @param timeFrom lower departure time in HH:mm format, optional
         * @param timeTo upper departure time in HH:mm format, optional
         * @param coachTypeKeyword coach type category keyword: LIMOUSINE, LUXURY, or TRUYEN_THONG
         * @param priceRanges checkbox values: LOW, MIDDLE, HIGH
         * @param statuses checked trip statuses, optional
         * @param driverName partial driver name search, optional
         * @param page zero-based page index
         * @param size rows per page
         * @return paged operational trip rows visible to ticket staff
         */
        PagedResponse<StaffTripInfoProjection> getStaffTripInfos(
                        LocalDate date,
                        String city,
                        String timeFrom,
                        String timeTo,
                        String coachTypeKeyword,
                        List<String> priceRanges,
                        List<String> statuses,
                        String driverName,
                        int page,
                        int size);

        /** Returns coaches that can serve the requested trip window. */
        List<TripResourceProjection> getAvailableCoaches(Integer routeId, LocalDateTime departureTime, Integer excludeTripId);

        /** Returns drivers that can serve the requested trip window. */
        List<TripResourceProjection> getAvailableDrivers(LocalDateTime departureTime, Integer excludeTripId);

        /** Returns attendants that can serve the requested trip window. */
        List<TripResourceProjection> getAvailableAttendants(LocalDateTime departureTime, Integer excludeTripId);

        /**
         * Loads the ordered route timeline for a concrete customer trip.
         *
         * @param tripId concrete trip identifier
         * @return active stops in route order
         */
        List<TripStopProjection> getTripStops(Integer tripId);

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
        /**
         * Executes the insert trip operation.
         *
         * @param tripRequest the value supplied for this operation
         *
         * @return the operation result
         */
        public String insertTrip(TripCreateRequest tripRequest);

        /**
         * Updates the trip.
         *
         * @param tripId the value supplied for this operation
         * @param updateRequest the value supplied for this operation
         *
         * @return the updated trip
         */
        public String updateTrip(Integer tripId, TripUpdateRequest updateRequest);

        /**
         * Deletes the trip.
         *
         * @param tripId the value supplied for this operation
         */
        public String deleteTrip(Integer tripId);

        /**
         * Finds the routes for dropdown.
         *
         * @return the matching result
         */
        public List<RouteDropdownDTO> findRoutesForDropdown();

        List<StaffProjection> getStaffNameDropDown(LocalDate date);

        List<CoachLicensePlateProjection> getCoachInfoDropDown(LocalDate date);

}
