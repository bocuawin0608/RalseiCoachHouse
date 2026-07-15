package com.ralsei.dto.request.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * UpdateRoleRequest
 */

/**
 * Represents the request payload for update role operations.
 */
public record UpdateRoleRequest(
    @NotBlank(message = "Tên vai trò không được để trống.")
    @Size(max = 50, message = "Tên vai trò không được vượt quá 50 ký tự.")
    String roleName,

    Boolean isActive
) {}
