package com.ralsei.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ralsei.model.AccountRole;

/**
 * Repository interface for {@link com.ralsei.model.AccountRole} entity.
 */

/**
 * Provides persistence access for account role data.
 */
public interface AccountRoleRepository extends JpaRepository<AccountRole, Integer> {
    List<AccountRole> findByAccountId(Integer accountId);
    void deleteByAccountId(Integer accountId);
}