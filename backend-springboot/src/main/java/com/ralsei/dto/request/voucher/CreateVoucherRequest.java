package com.ralsei.dto.request.voucher;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class CreateVoucherRequest {
    @NotBlank(message = "Voucher code is required")
    private String voucherCode;

    @NotBlank(message = "Discount type is required")
    @Pattern(regexp = "^(PERCENT|FIXED)$", message = "Discount type must be PERCENT or FIXED")
    private String discountType;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
    @DecimalMax(value = "100000000", message = "Discount value must not exceed 100,000,000")
    private BigDecimal discountValue;

    private BigDecimal maxDiscountValue;

    private BigDecimal minOrderValue;

    @NotNull(message = "Start effective date is required")
    private LocalDateTime startEffectiveDate;

    @NotNull(message = "End effective date is required")
    private LocalDateTime endEffectiveDate;

    @Min(value = 0, message = "Usage limit must be >= 0")
    private int usageLimit;
}
