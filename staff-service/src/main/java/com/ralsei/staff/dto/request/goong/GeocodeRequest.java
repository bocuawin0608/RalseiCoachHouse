package com.ralsei.staff.dto.request.goong;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the request payload for geocode operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class GeocodeRequest {
    @NotBlank(message = "Address is required")
    private String address;
}
