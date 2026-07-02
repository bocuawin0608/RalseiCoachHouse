package com.ralsei.dto.request.coachtype;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CoachTypeCreateRequest(
    @NotBlank(message = "Tên loại xe không được để trống.")
    @Size(max = 100, message = "Tên loại xe không được vượt quá 100 ký tự.")
    String coachTypeName,

    @NotBlank(message = "Không được để trống sơ đồ ghế.")
    @Size(max = 5000, message = "Dữ liệu sơ đồ ghế quá lớn, không hợp lệ.")
    String seatLayout,
    
    @NotNull(message = "Giá tiền không được để trống.")
    @PositiveOrZero(message = "Giá tiền không được nhỏ hơn 0.")
    @DecimalMax(value = "100000000", message = "Giá tiền không được vượt quá 100.000.000 đ.")
    BigDecimal seatPrice
) {}
