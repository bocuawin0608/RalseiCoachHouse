package com.ralsei.dto.response.voucher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Represents the response payload for voucher metrics operations.
 */
public class VoucherMetricsResponse {
    private long totalVouchers;
    private long activeVouchers;
    private long expiredVouchers;
    private long exhaustedVouchers;
    private long totalUsageCount;
}
