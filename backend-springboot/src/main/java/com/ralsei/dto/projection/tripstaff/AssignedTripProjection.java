/**
 * Projection for a trip assigned to the current staff member.
 * Includes route info, vehicle, and check-in progress summaries.
 */
package com.ralsei.dto.projection.tripstaff;

import java.time.LocalDateTime;

public interface AssignedTripProjection {

    Integer getTripId();

    String getRouteName();

    LocalDateTime getDepartureTime();

    String getLicensePlate();

    String getCoachTypeName();

    String getTripStatus();

    String getAssignedRole();

    Integer getTotalPassengers();

    Integer getCheckedInCount();
}
