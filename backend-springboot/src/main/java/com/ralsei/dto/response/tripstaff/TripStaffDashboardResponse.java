package com.ralsei.dto.response.tripstaff;

import java.util.List;

public record TripStaffDashboardResponse(
        TripStaffSummaryResponse tripSummary,
        List<TripStaffSeatResponse> seats,
        List<TripStaffPassengerResponse> passengers
) {}
