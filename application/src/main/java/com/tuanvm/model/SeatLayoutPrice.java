package com.tuanvm.model;

import java.math.BigDecimal;
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
@Table(name = "seat_layout_price")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatLayoutPrice extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seatLayoutPriceId")
    private int seatLayoutPriceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seatLayoutId", nullable = false)
    private int seatLayoutId;

    @Column(name = "seatPrice", nullable = false)
    private BigDecimal seatPrice;

    @Column(name = "startEffectiveDate", nullable = false)
    private LocalDateTime startEffectiveDate;

    @Column(name = "endEffectiveDate", nullable = false)
    private LocalDateTime endEffectiveDate;
}