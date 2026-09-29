package com.ralsei.staff.dto.response.cargoticket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the response payload for customer contact operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class CustomerContactResponse {
    private String phone;
    private String name;
}
