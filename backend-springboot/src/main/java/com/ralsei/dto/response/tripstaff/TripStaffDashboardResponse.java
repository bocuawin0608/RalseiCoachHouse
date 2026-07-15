/**
 * Aggregated dashboard data for a trip, including trip summary,
 * seat layout, and passenger list.
 */
package com.ralsei.dto.response.tripstaff;

import java.util.List;
import java.util.Set;

/**
 * Represents the response payload for trip staff dashboard operations.
 */
public record TripStaffDashboardResponse(
        TripStaffSummaryResponse tripSummary,
        List<TripStaffSeatResponse> seats,
        List<TripStaffPassengerResponse> passengers,
        Set<Integer> noShowTicketDetailIds
) {}
