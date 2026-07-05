package com.ralsei.dto.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
