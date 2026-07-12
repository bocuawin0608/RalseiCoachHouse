package com.ralsei.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import com.ralsei.dto.projection.AccountListProjection;
import com.ralsei.dto.projection.AccountProjection;
import com.ralsei.model.Account;

/**
 * Repository interface for {@link com.ralsei.model.Account} entity.
 */

public interface AccountRepository extends JpaRepository<Account, Integer> {
    
    @Query(value = """
        SELECT a.accountId    AS accountId,
            a.username     AS username,
            a.passwordHash AS passwordHash,
            a.firebaseUid  AS firebaseUid,
            a.authProvider AS authProvider,
            a.isActive     AS isActive,
            COALESCE(STRING_AGG(r.roleName, ','), 'CUSTOMER') AS roleNames
        FROM account a
        LEFT JOIN account_role ar ON a.accountId = ar.accountId
        LEFT JOIN role r          ON ar.roleId   = r.roleId
        WHERE a.username = :username
        GROUP BY a.accountId, a.username, a.passwordHash, 
                a.firebaseUid, a.authProvider, a.isActive
    """, nativeQuery = true)
    Optional<AccountProjection> findByUsernameWithRoles(@Param("username") String username);

    @Query(value = """
        SELECT a.accountId          AS accountId,
            a.username           AS username,
            a.authProvider       AS authProvider,
            a.isActive           AS isActive,
            CONVERT(VARCHAR, a.lastLogin, 120) AS lastLogin,
            COALESCE(STRING_AGG(r.roleName, ','), '') AS roleNames,
            s.staffId            AS staffId,
            s.staffName          AS staffName,
            s.staffPosition      AS staffPosition,
            s.phone              AS phone,
            s.email              AS email,
            CONVERT(VARCHAR, a.createdAt, 120) AS createdAt,
            c.customerName       AS customerName
        FROM account a
        LEFT JOIN account_role ar ON a.accountId = ar.accountId
        LEFT JOIN role r          ON ar.roleId   = r.roleId
        LEFT JOIN staff s         ON a.accountId = s.accountId
        LEFT JOIN customer c      ON a.accountId = c.accountId
        GROUP BY a.accountId, a.username, a.authProvider, a.isActive,
                a.lastLogin, a.createdAt,
                s.staffId, s.staffName, s.staffPosition, s.phone, s.email,
                c.customerName
        ORDER BY a.createdAt DESC
    """, nativeQuery = true)
    List<AccountListProjection> findAllAccountList();

    boolean existsByUsername(String username);
}
