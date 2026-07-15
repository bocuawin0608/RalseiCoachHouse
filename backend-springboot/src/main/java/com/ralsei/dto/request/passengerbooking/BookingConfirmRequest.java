package com.ralsei.dto.request.passengerbooking;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Represents the request payload for booking confirm operations.
 */
public record BookingConfirmRequest(
    @Min(value = 1, message = "Mã voucher không hợp lệ!")
    Integer voucherId,
    
    @NotNull(message = "Không được để trống điểm đón!")
    @Min(value = 1, message = "Điểm đón không hợp lệ!")
    Integer pickupStopId,

    @NotNull(message = "Không được để trống điểm trả!")
    @Min(value = 1, message = "Điểm trả không hợp lệ!")
    Integer dropoffStopId,

    @NotEmpty(message = "Danh sách hành khách không được để trống!")
    List<@Valid PassengerDTO> passengers
) {}
