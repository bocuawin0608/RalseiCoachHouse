package com.ralsei.staff.dto.response.goong;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents the response payload for geocode operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class GeocodeResponse {
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String formattedAddress;
}
