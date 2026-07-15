package com.ralsei.dto.request.goong;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Represents the request payload for geocode operations.
 */
public class GeocodeRequest {
    @NotBlank(message = "Address is required")
    private String address;
}
