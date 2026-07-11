package com.ralsei.dto.request.cargoticket;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TripByStopRequest {
    @NotNull(message = "Pickup stop is required")
    @Min(value = 1, message = "Pickup stop ID must be valid")
    private Integer pickupStopId;

    @NotNull(message = "Dropoff stop is required")
    @Min(value = 1, message = "Dropoff stop ID must be valid")
    private Integer dropoffStopId;
}
