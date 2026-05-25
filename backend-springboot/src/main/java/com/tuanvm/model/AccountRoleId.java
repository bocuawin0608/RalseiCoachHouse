package com.tuanvm.model;

import java.io.Serializable;
import java.util.Objects;

public class AccountRoleId implements Serializable {
    private int accountId;
    private int roleId;

    public AccountRoleId() {
    }

    public AccountRoleId(int accountId, int roleId) {
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

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        AccountRoleId that = (AccountRoleId) object;
        return accountId == that.accountId && roleId == that.roleId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, roleId);
    }
}