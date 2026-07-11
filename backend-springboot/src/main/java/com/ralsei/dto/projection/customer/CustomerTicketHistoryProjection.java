package com.ralsei.dto.projection.customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Flat database view used to assemble customer booking history and detail data.
 * One row represents one passenger seat so the service can preserve every QR.
 */
public interface CustomerTicketHistoryProjection {
    Integer getPassengerTicketId();
    Integer getTicketDetailId();
    String getTicketCode();
    String getTicketStatus();
    BigDecimal getTotalPrice();
    String getPickupStopName();
    String getDropoffStopName();
    LocalDateTime getBookedAt();
    LocalDateTime getDepartureTime();
    String getRouteName();
    String getCoachTypeName();
    String getPaymentMethod();
    String getPaymentStatus();
    String getTransactionId();
    LocalDateTime getPaymentExpiresAt();
    String getFullName();
    String getPhone();
    String getEmail();
    String getSeatCode();
    BigDecimal getSeatPrice();
}
