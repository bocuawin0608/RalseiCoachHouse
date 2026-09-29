package com.ralsei.staff.dto.request.cargoticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Destination office chooses how the receiver will pay before hand-off.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ReceiverPaymentMethodRequest {
    @NotBlank(message = "Payment method is required")
    @Pattern(regexp = "CASH|BANK_TRANSFER", message = "Payment method must be CASH or BANK_TRANSFER")
    private String paymentMethod;
}
