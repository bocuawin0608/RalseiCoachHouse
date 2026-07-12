package com.ralsei.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
@Table(name = "voucher")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "voucherId")
    private int voucherId;

    @Column(name = "voucherCode", nullable = false, unique = true)
    private String voucherCode;

    @Column(name = "discountValue", nullable = false)
    private BigDecimal discountValue;

    @Column(name = "startEffectiveDate", nullable = false)
    private LocalDateTime startEffectiveDate;

    @Column(name = "endEffectiveDate", nullable = false)
    private LocalDateTime endEffectiveDate;

    @Column(name = "discountType", nullable = false)
    private String discountType;

    @Column(name = "maxDiscountValue", nullable = false)
    private BigDecimal maxDiscountValue;

    @Column(name = "minOrderValue", nullable = false)
    private BigDecimal minOrderValue;

    @Column(name = "usageLimit", nullable = false)
    private int usageLimit;

    @Column(name = "usedCount", nullable = false)
    private int usedCount;
}