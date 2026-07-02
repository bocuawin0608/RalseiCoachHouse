package com.ralsei.dto.request.coach;

import jakarta.validation.constraints.Size;

public record CoachReactivateRequest(
    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự.")
    String reason
) {}
