package com.tuanvm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "account_role")
@IdClass(AccountRoleId.class)
public class AccountRole {

    @Id
    @Column(name = "accountId")
    private int accountId;

    @Id
    @Column(name = "roleId")
    private int roleId;

    public AccountRole() {
    }

    public AccountRole(int accountId, int roleId) {
        this.accountId = accountId;
        this.roleId = roleId;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }
}