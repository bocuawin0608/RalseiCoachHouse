package com.ralsei.driver.dto.projection;

public interface AccountProjection {
    Integer getAccountId();
    String getUsername();
    String getPasswordHash();
    String getFirebaseUid();
    String getAuthProvider();
    Boolean getIsActive();
    String getRoleNames();
}
