package com.ralsei.staff.dto.request.cargotype;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the request payload for cargo type operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CargoTypeRequest {
    @NotBlank(message = "Cargo type name is required")
    private String cargoTypeName;
}
