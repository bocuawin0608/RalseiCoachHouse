package com.ralsei.dto.projection;

/**
 * AccountListProjection
 */

public interface AccountListProjection {
    Integer getAccountId();
    String getUsername();
    String getAuthProvider();
    Boolean getIsActive();
    String getRoleNames();
    Integer getStaffId();
    String getStaffName();
    String getStaffPosition();
    String getPhone();
    String getEmail();
}
