package com.ralsei.dto.projection.staffrefund;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Flat passenger refund row loaded through {@code refund -> payment -> passenger_ticket}.
 */
/**
 * Projects the staff passenger refund ro data shape for query results.
 */
public interface StaffPassengerRefundRowProjection {
    Integer getRefundId();
    Integer getPaymentId();
    Integer getPassengerTicketId();
    BigDecimal getAmount();
    String getReason();
    String getRefundMethod();
    String getTransactionId();
    String getStatus();
    LocalDateTime getRefundTime();
    String getCallbackData();
    LocalDateTime getCreatedAt();
    Integer getCreatedBy();
    LocalDateTime getUpdatedAt();
    Integer getUpdatedBy();
    String getTicketCode();
    String getCustomerName();
    String getCustomerPhone();
}
