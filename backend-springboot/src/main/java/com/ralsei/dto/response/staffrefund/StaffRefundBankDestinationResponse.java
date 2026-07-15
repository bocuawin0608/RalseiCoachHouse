package com.ralsei.dto.response.staffrefund;

/**
 * Customer bank account details parsed from {@code refund.callbackData} for
 * manager-facing refund processing screens.
 *
 * @param bankName receiving bank name supplied during ticket cancellation
 * @param accountHolder account holder name on the receiving account
 * @param accountNumber numeric receiving account number
 */
/**
 * Represents the response payload for staff refund bank destination operations.
 */
public record StaffRefundBankDestinationResponse(
    String bankName,
    String accountHolder,
    String accountNumber
) {}
