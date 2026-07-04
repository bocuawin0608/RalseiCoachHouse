package com.ralsei.dto.response.goong;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculateRouteDistancesResponse {
    private String message;
    private int updated;
}
