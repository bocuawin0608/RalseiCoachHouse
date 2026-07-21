package com.ralsei.dto.request.cargoticket;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * Optional payment method when confirming destination hand-off for RECEIVER-paid orders.
 */
public class ConfirmReceivedRequest {
    @Pattern(regexp = "CASH|BANK_TRANSFER", message = "Payment method must be CASH or BANK_TRANSFER")
    private String paymentMethod;
}
