package com.ralsei.dto.request.coachtype;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Represents the request payload for coach type update info operations.
 */
public record CoachTypeUpdateInfoRequest(
    @NotBlank(message = "Tên loại xe không được để trống.")
    @Size(max = 100, message = "Tên loại xe không được vượt quá 100 ký tự.")
    String coachTypeName,
    
    @NotNull(message = "Trạng thái hoạt động của loại xe không được để trống.")
    Boolean isActive
) {}
