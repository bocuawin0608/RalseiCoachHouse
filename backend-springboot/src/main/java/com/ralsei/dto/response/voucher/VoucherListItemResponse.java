package com.ralsei.dto.response.voucher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Represents the response payload for voucher list item operations.
 */
public class VoucherListItemResponse {
    private int voucherId;
    private String voucherCode;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountValue;
    private BigDecimal minOrderValue;
    private LocalDateTime startEffectiveDate;
    private LocalDateTime endEffectiveDate;
    private LocalDateTime createdAt;
    private int usageLimit;
    private int usedCount;
    private String status;
}
