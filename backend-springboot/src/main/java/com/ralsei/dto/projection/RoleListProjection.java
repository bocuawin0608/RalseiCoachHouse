package com.ralsei.dto.projection;

/**
 * RoleListProjection
 */

public interface RoleListProjection {
    Integer getRoleId();
    String getRoleName();
    Boolean getIsActive();
    Long getAssignedCount();
}
