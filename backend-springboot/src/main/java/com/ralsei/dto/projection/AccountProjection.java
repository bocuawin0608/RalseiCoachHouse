package com.ralsei.dto.projection;

public interface AccountProjection {
    Integer getAccountId();
    String getUsername();
    String getPasswordHash();
    String getFirebaseUid();
    String getAuthProvider();  
    Boolean getIsActive();
    String getRoleNames();
    // cái địt con mẹ gì đây ơ địt mẹ thằng nào ném nó vào đây=)))))
}
