package com.ralsei.repository;

import com.ralsei.dto.projection.AccountProjection;
import com.ralsei.model.Account;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    
    @Query(value = """
        SELECT a.accountId    AS accountId,
            a.username     AS username,
            a.passwordHash AS passwordHash,
            a.firebaseUid  AS firebaseUid,
            a.authProvider AS authProvider,
            a.isActive     AS isActive,
            COALESCE(STRING_AGG(r.roleName, ','), 'Customer') AS roleName
        FROM account a
        LEFT JOIN account_role ar ON a.accountId = ar.accountId
        LEFT JOIN role r          ON ar.roleId   = r.roleId
        WHERE a.username = :username
        GROUP BY a.accountId, a.username, a.passwordHash, 
                a.firebaseUid, a.authProvider, a.isActive
    """, nativeQuery = true)
    Optional<AccountProjection> findByUsernameWithRoles(@Param("username") String username);

    boolean existsByUsername(String username);
}
