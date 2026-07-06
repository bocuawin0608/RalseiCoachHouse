package com.ralsei.dto.projection;

import java.time.LocalDateTime;

/**
 * CustomerListProjection
 */

public interface CustomerListProjection {
    Integer getCustomerId();
    String getCustomerName();
    String getPhone();
    String getEmail();
    LocalDateTime getCreatedAt();
    Boolean getIsActive();
}
