package com.tuanvm.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {
    @CreationTimestamp
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @Min(0)
    @Column(name = "createdBy", updatable = false)
    private Integer createdBy;

    @UpdateTimestamp
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Min(0)
    @Column(name = "updatedBy")
    private Integer updatedBy;
}

// sau dùng tới @LastModifiedBy + @CreatedBy dùng @EntityListeners(AuditingEntityListener.class) cần @EntityListeners(AuditingEntityListener.class) và tạo @Component public class AuditorAwareImpl implements AuditorAware<Integer>
