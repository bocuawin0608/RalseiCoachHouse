package com.ralsei.dto.response.passengerbooking;

import com.ralsei.model.enums.TripSeatStatus;

import lombok.Builder;

@Builder(toBuilder=true)
/**
 * Represents the response payload for trip seat operations.
 */
public record TripSeatResponse(
    Integer tripSeatId, 
    String seatCode,
    Integer rowIndex,
    Integer colIndex,
    Integer floorIndex,
    TripSeatStatus status
) {}
