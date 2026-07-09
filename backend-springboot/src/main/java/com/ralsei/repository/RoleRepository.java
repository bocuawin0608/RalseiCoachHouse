package com.ralsei.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ralsei.model.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(String roleName);
}
