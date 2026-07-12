package com.ralsei.dto.response.voucher;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherResponse {
    private int voucherId;
    private String voucherCode;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountValue;
    private BigDecimal minOrderValue;
    private LocalDateTime startEffectiveDate;
    private LocalDateTime endEffectiveDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int usageLimit;
    private int usedCount;
    private Integer createdBy;
    private Integer updatedBy;
    private String status;
}
