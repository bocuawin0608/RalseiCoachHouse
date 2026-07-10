package com.ralsei.dto.request.customer;

import java.time.LocalDate;

import com.ralsei.util.StringNormalize;
import com.ralsei.util.validation.BookingValidationPatterns;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Editable customer profile fields from the customer website.
 * Phone is intentionally omitted because this project authenticates customers
 * through Firebase phone OTP; changing it requires a separate verified-phone
 * flow, not a normal profile edit.
 *
 * @param customerName customer's display and booking name
 * @param email customer contact email
 * @param dob optional date of birth
 */
public record CustomerProfileUpdateRequest(
    @NotBlank(message = "Họ và tên không được để trống.")
    @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự.")
    @Pattern(
        regexp = BookingValidationPatterns.FULL_NAME,
        message = "Họ và tên không hợp lệ."
    )
    String customerName,

    @Size(max = BookingValidationPatterns.EMAIL_MAX_LENGTH, message = "Email không được vượt quá 254 ký tự.")
    @Pattern(
        regexp = BookingValidationPatterns.EMAIL,
        message = "Email không hợp lệ."
    )
    String email,

    @Past(message = "Ngày sinh phải là ngày trong quá khứ.")
    LocalDate dob
) {
    public CustomerProfileUpdateRequest {
        customerName = StringNormalize.trimToEmpty(customerName);
        email = StringNormalize.trimToNull(email);
    }
}
