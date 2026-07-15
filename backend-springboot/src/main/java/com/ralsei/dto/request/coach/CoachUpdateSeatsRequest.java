package com.ralsei.dto.request.coach;

import java.util.List;

import com.ralsei.util.validation.CoachValidationPatterns;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Represents the request payload for coach update seats operations.
 */
public record CoachUpdateSeatsRequest(
    @NotEmpty(message = "Danh sách ghế không được để trống.")
    @Valid
    List<SeatToggle> seats
) {
    /**
     * Provides the seat toggle component for the application.
     */
    public record SeatToggle(
        @NotNull @Positive Integer seatId,
        @NotNull Boolean isActive,

        @NotBlank(message = "Mã ghế không được để trống")
        @Size(max = 4, message = "Mã ghế không được vượt quá 4 ký tự")
        @Pattern(
            regexp = CoachValidationPatterns.SEAT_CODE,
            message = CoachValidationPatterns.SEAT_CODE_MESSAGE
        )
        String seatCode
    ) {}
}
