package com.ralsei.dto.request.payment;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCheckoutRequest {
    private Integer passengerTicketId;
    private Integer cargoTicketId;
    private BigDecimal amount;
    private String paymentMethod;
}
