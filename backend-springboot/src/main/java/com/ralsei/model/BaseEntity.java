package com.ralsei.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {
    @CreationTimestamp
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "createdBy", updatable = false)
    private Integer createdBy;

    @UpdateTimestamp
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "updatedBy")
    private Integer updatedBy;
}

// sau dùng tới @LastModifiedBy + @CreatedBy dùng @EntityListeners(AuditingEntityListener.class) cần @EntityListeners(AuditingEntityListener.class) và tạo @Component public class AuditorAwareImpl implements AuditorAware<Integer>
