package com.ralsei.dto.request.sePay;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SepayWebhookRequest {
    private Long id;
    private String gateway;
    private String transactionDate;
    private String accountNumber;
    private String subAccount;
    private String transferType;
    private BigDecimal transferAmount;
    private BigDecimal accumulated;
    private String content;
    private String referenceCode;
}
