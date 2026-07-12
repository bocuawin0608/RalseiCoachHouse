package com.ralsei.dto.request.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * CreateRoleRequest
 */

public record CreateRoleRequest(
    @NotBlank(message = "Tên vai trò không được để trống.")
    @Size(max = 50, message = "Tên vai trò không được vượt quá 50 ký tự.")
    String roleName
) {}
