package com.ralsei.dto.response.staffrefund;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Full passenger refund detail for manager review and payout confirmation.
 * Audit actor identifiers are resolved to human-readable staff display names.
 *
 * @param refundId internal refund identifier
 * @param paymentId originating payment identifier
 * @param ticketCode cancelled passenger ticket code linked to the refund
 * @param customerName primary passenger name on the cancelled ticket
 * @param customerPhone primary passenger phone on the cancelled ticket
 * @param amount refund amount queued or already paid out
 * @param status current refund workflow status
 * @param refundMethod outbound refund channel
 * @param reason business reason recorded when the refund was created
 * @param bankDestination parsed customer payout account details, may be {@code null}
 * @param transactionId payout reference recorded after staff confirmation
 * @param refundTime server timestamp when staff confirmed payout
 * @param createdAt timestamp when the refund request was created
 * @param createdByStaffDisplay resolved creator as {@code staffName - phone}, or fallback text
 * @param updatedAt timestamp of the latest refund update
 * @param updatedByStaffDisplay resolved updater as {@code staffName - phone}, or {@code null}
 */
/**
 * Represents the response payload for staff refund detail operations.
 */
public record StaffRefundDetailResponse(
    int refundId,
    int paymentId,
    String ticketCode,
    String customerName,
    String customerPhone,
    BigDecimal amount,
    String status,
    String refundMethod,
    String reason,
    StaffRefundBankDestinationResponse bankDestination,
    String transactionId,
    LocalDateTime refundTime,
    LocalDateTime createdAt,
    String createdByStaffDisplay,
    LocalDateTime updatedAt,
    String updatedByStaffDisplay
) {}
