package com.ralsei.dto.projection.staffpassengerticket;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface StaffPassengerTransferCandidateProjection {
    Integer getTripId();
    String getRouteName();
    String getCoachTypeName();
    LocalDateTime getDepartureTime();
    BigDecimal getSeatPrice();
    Integer getAvailableSeats();
    Integer getTotalSeats();
}
