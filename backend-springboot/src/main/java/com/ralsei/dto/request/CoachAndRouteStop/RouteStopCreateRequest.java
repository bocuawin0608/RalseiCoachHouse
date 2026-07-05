package com.ralsei.dto.request.CoachAndRouteStop;

import jakarta.validation.constraints.Max;
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
public class RouteStopCreateRequest {
    @NotNull(message = "Coach stop ID is required")
    @Min(value = 1, message = "Coach stop ID must be at least 1")
    @Max(value = 2147483647, message = "Coach stop ID must be less than 2147483647")
    private int stopPointId;

    @Min(value = 1, message = "Stop order must be at least 1")
    @Max(value = 2147483647, message = "Stop order must be less than 2147483647")
    private int stopOrder;

    private BigDecimal kilometersFromStart;

    private int minutesFromStart;
}
