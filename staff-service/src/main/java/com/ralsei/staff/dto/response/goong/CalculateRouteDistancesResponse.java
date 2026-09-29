package com.ralsei.staff.dto.response.goong;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the response payload for calculate route distances operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CalculateRouteDistancesResponse {
    private String message;
    private int updated;
}
