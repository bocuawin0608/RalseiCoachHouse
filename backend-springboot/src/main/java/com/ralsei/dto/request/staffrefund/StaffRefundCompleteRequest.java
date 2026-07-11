package com.ralsei.dto.request.staffrefund;

import jakarta.validation.constraints.Size;

/**
 * Manager confirmation payload for completing a pending passenger refund.
 * Final required-ness of {@link #transactionId()} is validated in the service
 * layer according to the persisted {@code refund.refundMethod}.
 *
 * @param transactionId payout reference supplied by manager; optional for cash refunds
 */
public record StaffRefundCompleteRequest(
    @Size(max = 100, message = "Mã giao dịch không được vượt quá 100 ký tự.")
    String transactionId
) {}
