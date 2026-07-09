package com.ralsei.dto.request.passengerbooking;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PriceCalculationRequest(
    @Size(max = 36, message = "holdToken không hợp lệ.")
    String holdToken,

    @Positive(message = "Điểm đón không hợp lệ.")
    Integer pickupStopId,

    @Positive(message = "Điểm trả không hợp lệ.")
    Integer dropoffStopId,

    @Positive(message = "Mã voucher không hợp lệ.")
    Integer voucherId
) {}
