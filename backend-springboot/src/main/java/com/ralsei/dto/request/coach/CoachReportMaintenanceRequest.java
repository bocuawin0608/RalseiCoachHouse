package com.ralsei.dto.request.coach;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CoachReportMaintenanceRequest(
    @NotBlank(message = "Lý do bảo trì không được để trống.")
    @Size(max = 500, message = "Lý do không được vượt quá 500 ký tự.")
    String reason,

    LocalDateTime expectedEndAt
) {}
