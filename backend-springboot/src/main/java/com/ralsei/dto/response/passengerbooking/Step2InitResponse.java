package com.ralsei.dto.response.passengerbooking;

import java.math.BigDecimal;
import java.util.List;

public record Step2InitResponse(
    List<CoachStopDropdownDTO> pickupStopPoints,
    List<CoachStopDropdownDTO> dropoffStopPoints,
    List<VoucherDTO> vouchers,
    BigDecimal totalPrice,
    BigDecimal basePrice
) {}
