package com.ralsei.dto.request.staffpassengerticket;

import com.ralsei.dto.request.passengerbooking.AccompaniedChildDTO;
import com.ralsei.util.validation.BookingValidationPatterns;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Represents the request payload for staff passenger change passenger operations.
 */
public record StaffPassengerChangePassengerRequest(
    @NotBlank(message = "Vui lòng nhập họ tên!")
    @Pattern(
        regexp = BookingValidationPatterns.FULL_NAME,
        message = "Họ tên không hợp lệ. Vui lòng nhập ít nhất 2 ký tự, chỉ gồm chữ cái và khoảng trắng!"
    )
    String fullName,

    @NotBlank(message = "Vui lòng nhập số điện thoại!")
    @Pattern(
        regexp = BookingValidationPatterns.PHONE,
        message = "Số điện thoại không hợp lệ. Vui lòng nhập 10 chữ số, bắt đầu bằng 0!"
    )
    String phone,

    @NotBlank(message = "Vui lòng nhập email!")
    @Pattern(
        regexp = BookingValidationPatterns.EMAIL,
        message = "Email không hợp lệ!"
    )
    String email,

    @Valid
    AccompaniedChildDTO accompaniedChild,

    Boolean removeAccompaniedChild
) {}
