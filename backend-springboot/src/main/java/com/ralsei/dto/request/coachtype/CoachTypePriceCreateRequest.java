package com.ralsei.dto.request.coachtype;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CoachTypePriceCreateRequest(
    @NotNull(message = "Giá tiền không được để trống.")
    @PositiveOrZero(message = "Giá tiền không được nhỏ hơn 0.")
    @DecimalMax(value = "100000000", message = "Giá tiền không được vượt quá 100.000.000 đ.")
    BigDecimal seatPrice,

    @NotNull(message = "Ngày bắt đầu hiệu lực không được để trống.")
    LocalDateTime startEffectiveDate,

    LocalDateTime endEffectiveDate
) {}
