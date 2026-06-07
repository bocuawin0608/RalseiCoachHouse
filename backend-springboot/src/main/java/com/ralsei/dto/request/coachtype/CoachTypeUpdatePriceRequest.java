package com.ralsei.dto.request.coachtype;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CoachTypeUpdatePriceRequest(
    @NotNull(message = "Giá tiền không được để trống.")
    @PositiveOrZero(message = "Giá tiền không được nhỏ hơn 0.")
    @DecimalMax(value = "100000000", message = "Giá tiền không được vượt quá 100.000.000 đ.")
    BigDecimal seatPrice
) {}
