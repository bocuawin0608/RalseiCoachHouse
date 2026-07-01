package com.ralsei.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.dto.projection.RoleListProjection;
import com.ralsei.dto.request.role.CreateRoleRequest;
import com.ralsei.dto.request.role.RoleFilterRequest;
import com.ralsei.dto.request.role.UpdateRoleRequest;
import com.ralsei.dto.response.role.RoleDetailResponse;
import com.ralsei.dto.response.role.RoleListResponse;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.Role;
import com.ralsei.repository.RoleRepository;
import com.ralsei.service.RoleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepo;

    @Override
    @Transactional(readOnly = true)
    public Page<RoleListResponse> filterRoles(RoleFilterRequest filterRequest, Pageable pageable) {
        String search = (filterRequest != null && filterRequest.search() != null && !filterRequest.search().isBlank())
            ? filterRequest.search().trim()
            : null;
        Boolean isActive = filterRequest != null ? filterRequest.isActive() : null;

        List<RoleListProjection> projections = roleRepo.filterRoles(search, isActive);

        List<RoleListResponse> responses = projections.stream()
            .map(this::mapToListResponse)
            .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), responses.size());
        List<RoleListResponse> pageContent = start < responses.size() ? responses.subList(start, end) : List.of();

        return new PageImpl<>(pageContent, pageable, responses.size());
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDetailResponse getRoleDetail(Integer roleId) {
        Role role = roleRepo.findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Vai trò không tồn tại!"));
        return mapToDetailResponse(role);
    }

    @Override
    @Transactional
    public Integer createRole(CreateRoleRequest request) {
        String roleName = request.roleName().trim();
        if (roleRepo.existsByRoleNameIgnoreCase(roleName)) {
            throw new BusinessRuleException("Tên vai trò này đã tồn tại trong hệ thống!");
        }

        Role role = Role.builder()
            .roleName(roleName)
            .isActive(true)
            .build();

        return roleRepo.save(role).getRoleId();
    }

    @Override
    @Transactional
    public void updateRole(Integer roleId, UpdateRoleRequest request) {
        Role role = roleRepo.findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Vai trò không tồn tại!"));

        String roleName = request.roleName().trim();
        if (!role.getRoleName().equalsIgnoreCase(roleName)
            && roleRepo.existsByRoleNameIgnoreCase(roleName)) {
            throw new BusinessRuleException("Tên vai trò này đã tồn tại trong hệ thống!");
        }

        if (request.isActive() != null && !request.isActive() && Boolean.TRUE.equals(role.getIsActive())) {
            ensureNotLastAdmin(roleId);
        }

        role.setRoleName(roleName);
        if (request.isActive() != null) {
            role.setIsActive(request.isActive());
        }
        roleRepo.save(role);
    }

    @Override
    @Transactional
    public void deleteRole(Integer roleId) {
        Role role = roleRepo.findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Vai trò không tồn tại!"));

        long actualAssigned = roleRepo.countByRoleId(roleId);
        if (actualAssigned > 0) {
            throw new BusinessRuleException(
                "Không thể xóa vai trò đang được gán cho " + actualAssigned + " tài khoản. Vui lòng gỡ gán trước!");
        }

        if ("ADMIN".equalsIgnoreCase(role.getRoleName())) {
            throw new BusinessRuleException("Không thể xóa vai trò ADMIN!");
        }

        roleRepo.delete(role);
    }

    @Override
    @Transactional
    public void toggleActive(Integer roleId) {
        Role role = roleRepo.findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Vai trò không tồn tại!"));

        if (Boolean.TRUE.equals(role.getIsActive())) {
            ensureNotLastAdmin(roleId);
        }

        role.setIsActive(!Boolean.TRUE.equals(role.getIsActive()));
        roleRepo.save(role);
    }

    private void ensureNotLastAdmin(Integer roleId) {
        Role target = roleRepo.findById(roleId).orElse(null);
        if (target == null || !"ADMIN".equalsIgnoreCase(target.getRoleName())) {
            return;
        }
        long activeAdminCount = roleRepo.findAll().stream()
            .filter(r -> "ADMIN".equalsIgnoreCase(r.getRoleName()))
            .filter(r -> Boolean.TRUE.equals(r.getIsActive()))
            .count();
        if (activeAdminCount <= 1) {
            throw new BusinessRuleException("Không thể vô hiệu hóa vai trò ADMIN cuối cùng!");
        }
    }

    private RoleListResponse mapToListResponse(RoleListProjection proj) {
        return new RoleListResponse(
            proj.getRoleId(),
            proj.getRoleName(),
            proj.getIsActive() != null && proj.getIsActive(),
            proj.getAssignedCount() != null ? proj.getAssignedCount() : 0L,
            null
        );
    }

    private RoleDetailResponse mapToDetailResponse(Role role) {
        return new RoleDetailResponse(
            role.getRoleId(),
            role.getRoleName(),
            Boolean.TRUE.equals(role.getIsActive()),
            roleRepo.countByRoleId(role.getRoleId()),
            role.getCreatedAt(),
            role.getCreatedBy(),
            role.getUpdatedAt(),
            role.getUpdatedBy()
        );
    }
}
