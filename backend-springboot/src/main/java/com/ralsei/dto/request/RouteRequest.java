package com.ralsei.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteRequest {
    @NotBlank(message = "Route name is required")
    private String routeName;

    @NotNull(message = "Total kilometers is required")
    @Positive(message = "Total kilometers must be greater than zero")
    private BigDecimal totalKilometers;

    @Min(value = 1, message = "Total minutes must be at least 1")
    private int totalMinutes;
}
