package com.ralsei.dto.request.staff;

import java.time.LocalDate;

import com.ralsei.util.validation.BookingValidationPatterns;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Editable staff profile fields from the internal staff website.
 * Phone, role, position, agency, and hire date are intentionally omitted
 * because they are operational identity fields owned by managers/admins.
 *
 * @param staffName staff member display name used in internal workflows
 * @param email optional staff contact email
 * @param dob optional date of birth; staff must still satisfy the 20+ age rule
 */
public record StaffProfileUpdateRequest(
    @NotBlank(message = "Họ và tên không được để trống.")
    @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự.")
    @Pattern(
        regexp = BookingValidationPatterns.FULL_NAME,
        message = "Họ và tên không hợp lệ."
    )
    String staffName,

    @Email(message = "Email không hợp lệ.")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự.")
    String email,

    @Past(message = "Ngày sinh phải là ngày trong quá khứ.")
    LocalDate dob
) {}
