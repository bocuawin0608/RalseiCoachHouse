/**
 * Seat information on the trip seat map,
 * including occupancy status and assigned passenger name.
 */
package com.ralsei.dto.response.tripstaff;

import com.ralsei.model.enums.TripSeatStatus;

/**
 * Represents the response payload for trip staff seat operations.
 */
public record TripStaffSeatResponse(
        Integer tripSeatId,
        String seatCode,
        Integer rowIndex,
        Integer colIndex,
        Integer floorIndex,
        TripSeatStatus tripSeatStatus,
        String passengerDetailStatus,
        String fullName,
        boolean noShow
) {}
