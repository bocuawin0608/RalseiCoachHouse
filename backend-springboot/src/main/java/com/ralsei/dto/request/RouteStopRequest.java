package com.ralsei.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteStopRequest {
    @NotNull(message = "Route ID is required")
    private Integer routeId;

    @NotNull(message = "Coach stop ID is required")
    private Integer stopPointId;

    @Min(value = 1, message = "Stop order must be at least 1")
    private int stopOrder;

    @NotNull(message = "Kilometers from start is required")
    @Min(value = 0, message = "Kilometers from start cannot be negative")
    private BigDecimal kilometersFromStart;

    @Min(value = 0, message = "Minutes from start cannot be negative")
    private int minutesFromStart;
}
