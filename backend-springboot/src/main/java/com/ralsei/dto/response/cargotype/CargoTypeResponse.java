package com.ralsei.dto.response.cargotype;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CargoTypeResponse {
    private int cargoTypeId;
    private String cargoTypeName;
    private boolean isActive;
}
