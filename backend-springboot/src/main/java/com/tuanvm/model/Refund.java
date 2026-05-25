package com.tuanvm.model;

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
@Table(name = "refund")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refund extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refundId")
    private int refundId;

    @Column(name = "paymentId", nullable = false)
    private int paymentId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "reason", columnDefinition = "NVARCHAR(MAX)")
    private String reason;

    @Column(name = "refundMethod", nullable = false)
    private String refundMethod;

    @Column(name = "transactionId")
    private String transactionId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "refundTime")
    private LocalDateTime refundTime;

    @Column(name = "callbackData", columnDefinition = "NVARCHAR(MAX)")
    private String callbackData;

 
}