package com.ralsei.dto.projection;

/**
 * RoleListProjection
 */

/**
 * Projects the role lis data shape for query results.
 */
public interface RoleListProjection {
    Integer getRoleId();
    String getRoleName();
    Boolean getIsActive();
    Long getAssignedCount();
}
