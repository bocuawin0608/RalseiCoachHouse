package com.ralsei.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "refresh_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Provides the refresh token component for the application.
 */
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, unique = true, length = 512)
    private String token;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accountId", nullable = false)
    private Account account;
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    @Builder.Default
    @Column(nullable = false)
    private Boolean isRevoked = false;

    /**
     * Returns whether the expired is active.
     *
     * @return {@code true} if the expired is active; otherwise {@code false}
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Returns whether the valid is active.
     *
     * @return {@code true} if the valid is active; otherwise {@code false}
     */
    public boolean isValid() {
        return !isRevoked && !isExpired();
    }
}
