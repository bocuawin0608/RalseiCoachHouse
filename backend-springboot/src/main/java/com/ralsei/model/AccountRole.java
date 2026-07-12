package com.ralsei.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "account_role")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * AccountRole entity.
 */

public class AccountRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "accountRoleId")
    private Integer accountRoleId;

    @Column(name = "accountId", nullable = false)
    private Integer accountId;

    @Column(name = "roleId", nullable = false)
    private Integer roleId;

}