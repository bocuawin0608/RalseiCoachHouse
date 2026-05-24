package com.tuanvm.model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int accountID;

    private String username;

    private String passwordHash;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "create_by")
    private int createBy;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "update_by")
    private int updateBy;

    public Account() {
    }

    public Account(int accountID, String username, String passwordHash, boolean isActive, LocalDateTime lastLogin,
            LocalDateTime createAt, int createBy, LocalDateTime updateAt, int updateBy) {
        this.accountID = accountID;
        this.username = username;
        this.passwordHash = passwordHash;
        this.isActive = isActive;
        this.lastLogin = lastLogin;
        this.createAt = createAt;
        this.createBy = createBy;
        this.updateAt = updateAt;
        this.updateBy = updateBy;
    }

    public int getAccountID() {
        return accountID;
    }

    public void setAccountID(int accountID) {
        this.accountID = accountID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
    }

    public int getCreateBy() {
        return createBy;
    }

    public void setCreateBy(int createBy) {
        this.createBy = createBy;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
    }

    public int getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(int updateBy) {
        this.updateBy = updateBy;
    }
}
