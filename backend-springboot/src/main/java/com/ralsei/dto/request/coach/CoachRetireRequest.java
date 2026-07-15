package com.ralsei.dto.request.coach;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Represents the request payload for coach retire operations.
 */
public record CoachRetireRequest(
    @NotBlank(message = "Lý do ngừng sử dụng không được để trống.")
    @Size(max = 500, message = "Lý do không được vượt quá 500 ký tự.")
    String reason
) {}
