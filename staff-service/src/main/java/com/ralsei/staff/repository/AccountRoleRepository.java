package com.ralsei.staff.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ralsei.staff.model.AccountRole;

public interface AccountRoleRepository extends JpaRepository<AccountRole, Integer> {
    List<AccountRole> findByAccountId(Integer accountId);
    void deleteByAccountId(Integer accountId);
}