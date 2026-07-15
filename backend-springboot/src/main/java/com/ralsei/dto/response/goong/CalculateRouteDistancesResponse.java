package com.ralsei.dto.response.goong;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Represents the response payload for calculate route distances operations.
 */
public class CalculateRouteDistancesResponse {
    private String message;
    private int updated;
}
