package com.ralsei.dto.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Projection for the native query that lists a customer's cargo orders.
 */
public interface CargoHistoryListProjection {
    Integer getCargoTicketId();
    String getTicketCode();
    String getStatus();
    String getSenderName();
    String getSenderPhone();
    String getReceiverName();
    String getReceiverPhone();
    BigDecimal getTotalPrice();
    LocalDateTime getCreatedAt();
    String getPickupStopName();
    String getDropoffStopName();
    String getTripRouteName();
    LocalDateTime getTripDepartureTime();
}
