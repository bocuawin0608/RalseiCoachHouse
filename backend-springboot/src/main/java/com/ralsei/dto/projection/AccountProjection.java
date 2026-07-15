package com.ralsei.dto.projection;

/**
 * AccountProjection
 */

/**
 * Projects the accoun data shape for query results.
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
