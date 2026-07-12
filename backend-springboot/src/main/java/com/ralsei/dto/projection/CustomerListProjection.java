package com.ralsei.dto.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface CustomerListProjection {
    Integer getCustomerId();
    String getCustomerName();
    String getPhone();
    String getEmail();
    LocalDateTime getCreatedAt();
    Boolean getIsActive();
    Integer getAccountId();
    Long getTotalTrips();
    BigDecimal getTotalSpent();
    LocalDateTime getLastBooking();
}