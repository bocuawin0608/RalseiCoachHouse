package com.ralsei.customer.dto.projection;

public interface AccountProjection {
    Integer getAccountId();
    String getUsername();
    String getPasswordHash();
    String getFirebaseUid();
    String getAuthProvider();
    Boolean getIsActive();
    String getRoleNames();
}
