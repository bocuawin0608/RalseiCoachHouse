package com.ralsei.dto.request.coach;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CoachUpdateSeatsRequest(
    @NotEmpty(message = "Danh sách ghế không được để trống.")
    @Valid
    List<SeatToggle> seats
) {
    public record SeatToggle(
        @NotNull @Positive Integer seatId,
        
        @NotNull Boolean isActive,

        @NotBlank(message = "Mã ghế không được để trống")
        @Size(min = 1, max = 10, message = "Mã ghế phải có độ dài từ 1 đến 10 ký tự")
        String seatCode
    ) {}
}
