package com.ralsei.dto.request.passengerbooking;

import java.time.Year;

import com.ralsei.util.validation.BookingValidationPatterns;

import jakarta.validation.constraints.AssertTrue;
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
    Integer birthYear
) {
    @AssertTrue(message = "Trẻ em đi kèm phải từ 0 đến 6 tuổi.")
    public boolean isBirthYearValid() {
        if (birthYear == null) return true;
        int currentYear = Year.now().getValue();
        return birthYear >= currentYear - 6 && birthYear <= currentYear;
    }
}
