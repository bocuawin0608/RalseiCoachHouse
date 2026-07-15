package com.ralsei.dto.request.coachtype;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Represents the request payload for coach type price create operations.
 */
public record CoachTypePriceCreateRequest(
    @NotNull(message = "Giá tiền không được để trống.")
    @PositiveOrZero(message = "Giá tiền không được nhỏ hơn 0.")
    @DecimalMax(value = "100000000", message = "Giá tiền không được vượt quá 100.000.000 đ.")
    BigDecimal seatPrice,

    @NotNull(message = "Ngày bắt đầu hiệu lực không được để trống.")
    LocalDateTime startEffectiveDate,

    LocalDateTime endEffectiveDate
) {
    @AssertTrue(message = "Ngày bắt đầu hiệu lực không được ở quá khứ.")
    /**
     * Returns whether the start effective date valid is active.
     *
     * @return {@code true} if the start effective date valid is active; otherwise {@code false}
     */
    public boolean isStartEffectiveDateValid() {
        return startEffectiveDate == null
            || !startEffectiveDate.isBefore(LocalDateTime.now().minusMinutes(5));
    }

    @AssertTrue(message = "Ngày kết thúc hiệu lực phải sau ngày bắt đầu.")
    /**
     * Returns whether the end effective date valid is active.
     *
     * @return {@code true} if the end effective date valid is active; otherwise {@code false}
     */
    public boolean isEndEffectiveDateValid() {
        return endEffectiveDate == null
            || startEffectiveDate == null
            || endEffectiveDate.isAfter(startEffectiveDate);
    }
}
