package com.ralsei.dto.request.coach;

import com.ralsei.util.validation.CoachValidationPatterns;
import com.ralsei.util.validation.MaxCurrentYear;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Represents the request payload for coach create operations.
 */
public record CoachCreateRequest(

    @NotNull(message = "ID loại xe không được để trống.")
    @Positive(message = "ID loại xe phải là số dương.")
    Integer coachTypeId,
    
    @Positive(message = "ID tuyến đường phải là số dương.")

    Integer routeId,

    @NotBlank(message = "Biển số xe không được để trống.")
    @Size(max = 20, message = "Biển số xe không được vượt quá 20 ký tự.")
    @Pattern(
        regexp = CoachValidationPatterns.LICENSE_PLATE_INPUT,
        message = CoachValidationPatterns.LICENSE_PLATE_MESSAGE
    )
    String licensePlate,

    @NotBlank(message = "Hãng xe không được để trống.")
    @Size(max = 100, message = "Hãng xe không được vượt quá 100 ký tự.")
    String manufacturer,

    @NotNull(message = "Năm sản xuất không được để trống.")
    @Min(value = CoachValidationPatterns.YEAR_MIN, message = "Năm sản xuất phải lớn hơn hoặc bằng 2000.")
    @MaxCurrentYear
    Integer year

) {}
