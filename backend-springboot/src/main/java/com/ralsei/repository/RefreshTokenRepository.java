package com.ralsei.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.model.Account;
import com.ralsei.model.RefreshToken;

/**
 * Provides persistence access for refresh token data.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    Optional<RefreshToken> findByToken(String token);

    // Xóa toàn bộ refresh token của 1 account (khi logout all devices)
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.account = :account")
    void deleteAllByAccount(Account account);

    // Revoke tất cả token của account (khi đổi password)
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.account = :account")
    void revokeAllByAccount(Account account);
}