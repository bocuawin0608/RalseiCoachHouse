package com.ralsei.dto.projection;

public interface AccountListProjection {
    Integer getAccountId();
    String getUsername();
    String getAuthProvider();
    Boolean getIsActive();
    String getLastLogin();
    String getRoleNames();
    String getCreatedAt();
}
