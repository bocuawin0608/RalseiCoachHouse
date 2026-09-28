package com.ralsei.auth.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ralsei.auth.dto.request.role.CreateRoleRequest;
import com.ralsei.auth.dto.request.role.RoleFilterRequest;
import com.ralsei.auth.dto.request.role.UpdateRoleRequest;
import com.ralsei.auth.dto.response.role.RoleDetailResponse;
import com.ralsei.auth.dto.response.role.RoleListResponse;

/**
 * Service interface for role management operations.
 */

/**
 * Provides the business service contract for role.
 */
public interface RoleService {
    Page<RoleListResponse> filterRoles(RoleFilterRequest filterRequest, Pageable pageable);
    RoleDetailResponse getRoleDetail(Integer roleId);
    Integer createRole(CreateRoleRequest request);
    void updateRole(Integer roleId, UpdateRoleRequest request);
    void deleteRole(Integer roleId);
    void toggleActive(Integer roleId);
}
