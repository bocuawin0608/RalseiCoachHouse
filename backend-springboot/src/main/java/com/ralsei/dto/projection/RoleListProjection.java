package com.ralsei.dto.projection;

public interface RoleListProjection {
    Integer getRoleId();
    String getRoleName();
    Boolean getIsActive();
    Long getAssignedCount();
}
