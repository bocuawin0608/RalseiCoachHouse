package com.ralsei.dto.request.coach;

import java.time.LocalDateTime;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Represents the request payload for coach report maintenance operations.
 */
public record CoachReportMaintenanceRequest(
    @NotBlank(message = "Lý do bảo trì không được để trống.")
    @Size(max = 500, message = "Lý do không được vượt quá 500 ký tự.")
    String reason,

    LocalDateTime expectedEndAt
) {
    @AssertTrue(message = "Thời gian dự kiến hoàn thành phải ở tương lai.")
    /**
     * Returns whether the expected end at valid is active.
     *
     * @return {@code true} if the expected end at valid is active; otherwise {@code false}
     */
    public boolean isExpectedEndAtValid() {
        return expectedEndAt == null || expectedEndAt.isAfter(LocalDateTime.now());
    }
}
