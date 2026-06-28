package com.ralsei.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payment")
@org.hibernate.annotations.Check(constraints = "(passengerTicketId IS NOT NULL AND cargoTicketId IS NULL) OR (passengerTicketId IS NULL AND cargoTicketId IS NOT NULL)")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paymentId")
    private int paymentId;

    @Column(name = "passengerTicketId")
    private Integer passengerTicketId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargoTicketId")
    private CargoTicket cargoTicket;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "paymentMethod", nullable = false)
    private String paymentMethod;

    @Column(name = "transactionId", nullable = false, unique = true)
    private String transactionId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "refundAmount", nullable = false)
    private BigDecimal refundAmount;

    @Column(name = "paymentTime")
    private LocalDateTime paymentTime;

    @Column(name = "callbackData", columnDefinition = "NVARCHAR(MAX)")
    private String callbackData;
}