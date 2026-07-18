package com.ralsei.dto.request.passengerbooking;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Represents the request payload for seat lock operations.
 * {@code vacateTripSeatId} / {@code restoreVacatedTripSeatIds} are staff same-trip
 * change helpers; booking may omit them.
 */
public record SeatLockRequest(
    @NotEmpty(message = "Phải chọn ít nhất 1 ghế.")
    @Size(max = 10, message = "Chỉ được chọn tối đa 10 ghế.")
    List<@NotNull @Positive Integer> tripSeatIds,
    @Positive Integer vacateTripSeatId,
    List<@Positive Integer> restoreVacatedTripSeatIds
) {
    public SeatLockRequest {
        if (restoreVacatedTripSeatIds == null) {
            restoreVacatedTripSeatIds = List.of();
        }
    }

    /** Booking-style constructor (tripSeatIds only). */
    public SeatLockRequest(List<Integer> tripSeatIds) {
        this(tripSeatIds, null, List.of());
    }
}
