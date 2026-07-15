package com.ralsei.dto.response.staffrefund;

import java.math.BigDecimal;

/**
 * One passenger refund row for manager list and search results. The identifier is
 * returned for client-side detail navigation but is not intended for display.
 *
 * @param refundId internal refund identifier used by detail and complete APIs
 * @param ticketCode cancelled passenger ticket code linked to the refund
 * @param customerName primary passenger name on the cancelled ticket
 * @param customerPhone primary passenger phone on the cancelled ticket
 * @param amount refund amount queued for payout
 * @param status current refund workflow status
 * @param refundMethod outbound refund channel selected when the refund was created
 * @param reason business reason recorded when the refund request was created
 */
/**
 * Represents the response payload for staff refund list item operations.
 */
public record StaffRefundListItemResponse(
    int refundId,
    String ticketCode,
    String customerName,
    String customerPhone,
    BigDecimal amount,
    String status,
    String refundMethod,
    String reason
) {}
