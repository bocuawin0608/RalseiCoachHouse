package com.ralsei.dto.request.seatlayout;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record SeatLayoutUpdateSeatRequest(
    @NotNull(message = "Danh sách ghế không được để trống")
    List<@Valid SeatRequestDTO> seats
) {}
