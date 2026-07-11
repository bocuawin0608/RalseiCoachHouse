package com.ralsei.model.enums;

import com.ralsei.exception.BusinessRuleException;

/**
 * Lifecycle states persisted in {@code refund.status} and enforced by the
 * database {@code CK_Refund_Status} constraint.
 */
public enum RefundStatus {
    /** Refund request recorded and awaiting staff payout confirmation. */
    PENDING,
    /** Staff confirmed that the refund amount was paid to the customer. */
    COMPLETED,
    /** Refund could not be completed; reserved for future failure handling. */
    FAILED;

    /**
     * Parses a trimmed status value from API input or persisted rows.
     *
     * @param value raw status text, may be {@code null}
     * @return matching enum constant, or {@code null} when input is blank
     * @throws BusinessRuleException when the value is not a supported status
     */
    public static RefundStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        try {
            return RefundStatus.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            throw new BusinessRuleException("Trạng thái hoàn tiền không hợp lệ.");
        }
    }
}
