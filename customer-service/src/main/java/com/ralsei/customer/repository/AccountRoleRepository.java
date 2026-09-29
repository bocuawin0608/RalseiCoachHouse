package com.ralsei.customer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ralsei.customer.model.AccountRole;

public interface AccountRoleRepository extends JpaRepository<AccountRole, Integer> {
    List<AccountRole> findByAccountId(Integer accountId);
    void deleteByAccountId(Integer accountId);
}