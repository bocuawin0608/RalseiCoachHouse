package com.ralsei.dto.request.passengerbooking;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record BookingConfirmRequest(
    Integer voucherId,
    
    @NotNull(message = "Không được để trống điểm đón!")
    Integer pickupStopId,

    @NotNull(message = "Không được để trống điểm trả!")
    Integer dropoffStopId,

    @NotEmpty(message = "Danh sách hành khách không được để trống!")
    List<PassengerDTO> passengers
) {}
