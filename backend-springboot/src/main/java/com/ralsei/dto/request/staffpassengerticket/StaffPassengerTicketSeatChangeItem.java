package com.ralsei.dto.request.staffpassengerticket;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * One seat reassignment inside a staff ticket change session.
 */
public record StaffPassengerTicketSeatChangeItem(
    @NotNull(message = "Mã ghế không được để trống!")
    @Min(value = 1, message = "Mã ghế không hợp lệ!")
    Integer ticketDetailId,

    @NotNull(message = "Mã ghế mới không được để trống!")
    @Min(value = 1, message = "Mã ghế mới không hợp lệ!")
    Integer newTripSeatId
) {}
