package com.ralsei.dto.request.passengerbooking;

import com.ralsei.util.validation.BookingValidationPatterns;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AccompaniedChildDTO(
    @NotBlank(message = "Vui lòng nhập tên bé!")
    @Pattern(
        regexp = BookingValidationPatterns.FULL_NAME,
        message = "Họ tên bé không hợp lệ. Vui lòng nhập ít nhất 2 ký tự, chỉ gồm chữ cái và khoảng trắng!"
    )
    String fullname,

    @NotNull(message = "Vui lòng nhập năm sinh của bé!")
    @Min(value = 2010, message = "Năm sinh phải >= 2010!")
    @Max(value = 2100, message = "Năm sinh phải <= năm hiện tại!")
    Integer birthYear
) {}
