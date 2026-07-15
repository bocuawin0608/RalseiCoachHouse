package com.ralsei.dto.request.staffpassengerticket;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Represents the request payload for staff passenger change seat operations.
 */
public record StaffPassengerChangeSeatRequest(
    @NotNull(message = "Mã ghế mới không được để trống!")
    @Min(value = 1, message = "Mã ghế mới không hợp lệ!")
    Integer newTripSeatId
) {}
