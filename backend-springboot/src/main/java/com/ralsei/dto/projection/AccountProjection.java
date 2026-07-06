package com.ralsei.dto.projection;

/**
 * AccountProjection
 */

public interface AccountProjection {
    Integer getAccountId();
    String getUsername();
    String getPasswordHash();
    String getFirebaseUid();
    String getAuthProvider();  
    Boolean getIsActive();
    String getRoleNames();
}
