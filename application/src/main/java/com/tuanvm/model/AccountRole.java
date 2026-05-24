package com.tuanvm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "account_role")
@IdClass(AccountRoleId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountRole {

    @Id
    @Column(name = "accountId")
    private int accountId;

    @Id
    @Column(name = "roleId")
    private int roleId;

}