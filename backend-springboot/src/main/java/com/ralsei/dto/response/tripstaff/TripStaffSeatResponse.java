package com.ralsei.dto.response.tripstaff;

import com.ralsei.model.enums.TripSeatStatus;

public record TripStaffSeatResponse(
        Integer tripSeatId,
        String seatCode,
        Integer rowIndex,
        Integer colIndex,
        Integer floorIndex,
        TripSeatStatus tripSeatStatus,
        String passengerDetailStatus,
        String fullName
) {}
