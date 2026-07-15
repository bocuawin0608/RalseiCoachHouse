package com.ralsei.dto.request.coachtype;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Represents the request payload for coach type update seatmap operations.
 */
public record CoachTypeUpdateSeatmapRequest(
    @NotBlank(message = "Không được để trống sơ đồ ghế.")
    @Size(max = 5000, message = "Dữ liệu sơ đồ ghế quá lớn, không hợp lệ.")
    String seatLayout
) {}
