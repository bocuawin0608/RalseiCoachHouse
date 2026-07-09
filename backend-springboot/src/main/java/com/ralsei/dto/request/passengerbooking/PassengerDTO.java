package com.ralsei.dto.request.passengerbooking;

import com.ralsei.util.validation.BookingValidationPatterns;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PassengerDTO(
    @NotNull(message = "Mã ghế không được để trống!")
    @Min(value = 1, message = "Mã ghế không hợp lệ!")
    Integer tripSeatId,

    @NotBlank(message = "Vui lòng nhập họ tên!")
    @Pattern(
        regexp = BookingValidationPatterns.FULL_NAME,
        message = "Họ tên không hợp lệ. Vui lòng nhập ít nhất 2 ký tự, chỉ gồm chữ cái và khoảng trắng!"
    )
    String fullname,

    @NotBlank(message = "Vui lòng nhập số điện thoại!")
    @Pattern(
        regexp = BookingValidationPatterns.PHONE,
        message = "Số điện thoại không hợp lệ. Vui lòng nhập 10 chữ số, bắt đầu bằng 0!"
    )
    String phone,

    @NotBlank(message = "Vui lòng nhập email!")
    @Size(max = BookingValidationPatterns.EMAIL_MAX_LENGTH, message = "Email không được vượt quá 254 ký tự.")
    @Pattern(
        regexp = BookingValidationPatterns.EMAIL,
        message = "Email không hợp lệ!"
    )
    String email,

    @Valid
    AccompaniedChildDTO accompaniedChild,

    @Size(max = 2048, message = "Token xác thực không hợp lệ.")
    String firebaseIdToken
) {}
