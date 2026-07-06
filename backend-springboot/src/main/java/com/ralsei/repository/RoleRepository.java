package com.ralsei.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import com.ralsei.dto.projection.RoleListProjection;
import com.ralsei.model.Role;

/**
 * Repository interface for {@link com.ralsei.model.Role} entity.
 */

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(String roleName);

    boolean existsByRoleNameIgnoreCase(String roleName);

    @Query(value = """
        SELECT r.roleId      AS roleId,
            r.roleName    AS roleName,
            r.isActive    AS isActive,
            (SELECT COUNT(*) FROM account_role ar WHERE ar.roleId = r.roleId) AS assignedCount
        FROM role r
        WHERE (:search IS NULL OR LOWER(r.roleName) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:isActive IS NULL OR r.isActive = :isActive)
        ORDER BY r.roleId ASC
    """, nativeQuery = true)
    List<RoleListProjection> filterRoles(@Param("search") String search, @Param("isActive") Boolean isActive);

    @Query(value = "SELECT COUNT(*) FROM account_role ar WHERE ar.roleId = :roleId", nativeQuery = true)
    long countByRoleId(@Param("roleId") Integer roleId);
}
