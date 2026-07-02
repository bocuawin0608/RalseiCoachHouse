package com.ralsei.dto.request.coach;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CoachUpdateSeatsRequest(
    @NotEmpty(message = "Danh sách ghế không được để trống.")
    @Valid
    List<SeatToggle> seats
) {
    public record SeatToggle(
        @NotNull @Positive Integer seatId,
        @NotNull Boolean isActive
    ) {}
}
