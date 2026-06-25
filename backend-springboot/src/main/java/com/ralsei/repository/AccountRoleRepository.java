package com.ralsei.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ralsei.model.AccountRole;

public interface AccountRoleRepository extends JpaRepository<AccountRole, Integer> {
    
}