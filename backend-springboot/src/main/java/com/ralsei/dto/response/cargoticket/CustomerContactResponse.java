package com.ralsei.dto.response.cargoticket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * Represents the response payload for customer contact operations.
 */
public class CustomerContactResponse {
    private String phone;
    private String name;
}
