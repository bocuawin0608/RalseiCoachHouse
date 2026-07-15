package com.ralsei.dto.request.staff;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Staff-only password change command.
 * The current password is required so a stolen active session cannot silently
 * replace the local password without knowing the existing secret.
 *
 * @param currentPassword staff member's existing local password
 * @param newPassword replacement password meeting the internal strength rule
 */
/**
 * Represents the request payload for staff password change operations.
 */
public record StaffPasswordChangeRequest(
    @NotBlank(message = "Mật khẩu hiện tại không được để trống.")
    @Size(max = 72, message = "Mật khẩu hiện tại không được vượt quá 72 ký tự.")
    String currentPassword,

    @NotBlank(message = "Mật khẩu mới không được để trống.")
    @Size(min = 8, max = 72, message = "Mật khẩu mới phải từ 8 đến 72 ký tự.")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
        message = "Mật khẩu mới phải có ít nhất một chữ cái và một chữ số."
    )
    String newPassword
) {}
