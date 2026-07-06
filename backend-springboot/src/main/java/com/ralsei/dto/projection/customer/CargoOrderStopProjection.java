package com.ralsei.dto.projection.customer;

import java.time.LocalDateTime;

/** Ordered stop row for the trip assigned to a phone-owned cargo order. */
public interface CargoOrderStopProjection {
    Integer getCargoTicketId();
    Integer getStopPointId();
    String getStopPointName();
    String getAddress();
    String getCity();
    Integer getStopOrder();
    LocalDateTime getEstimatedStopTime();
}
