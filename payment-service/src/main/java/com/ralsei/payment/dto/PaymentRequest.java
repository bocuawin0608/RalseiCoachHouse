package com.ralsei.payment.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private String orderId;
    private long amount;
    private String orderInfo;
}
