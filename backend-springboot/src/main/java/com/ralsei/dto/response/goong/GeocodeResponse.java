package com.ralsei.dto.response.goong;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Represents the response payload for geocode operations.
 */
public class GeocodeResponse {
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String formattedAddress;
}
