package com.ralsei.dto.response.passengerbooking;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents the data transfer object for voucher.
 */
public record VoucherDTO(
    Integer voucherId,
    String voucherCode,
    String discountType,      
    BigDecimal discountValue, 
    BigDecimal maxDiscountValue, 
    BigDecimal minOrderValue,    
    LocalDateTime endEffectiveDate
) {}
