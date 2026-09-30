package com.ralsei.auth.dto.projection;

public interface AccountListProjection {
    Integer getAccountId();
    String getUsername();
    String getAuthProvider();
    Boolean getIsActive();
    String getLastLogin();
    String getRoleNames();
    String getCreatedAt();
    default Integer getStaffId() { return null; }
    default String getStaffName() { return null; }
    default String getStaffPosition() { return null; }
    default String getPhone() { return null; }
    default String getEmail() { return null; }
    default String getCustomerName() { return null; }
}
