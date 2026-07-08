package com.ralsei.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import java.util.List;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cargo_ticket")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CargoTicket extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cargoTicketId")
    private int cargoTicketId;

    // thieu mapping one to many voi trip
    @Column(name = "tripId")
    private Integer tripId;

    @Column(name = "customerId")
    private Integer customerId;

    @Column(name = "senderName", nullable = false)
    private String senderName;

    @Column(name = "senderPhone", nullable = false)
    private String senderPhone;

    @Column(name = "receiverName", nullable = false)
    private String receiverName;

    @Column(name = "receiverPhone", nullable = false)
    private String receiverPhone;

    @Column(name = "ticketCode", nullable = false, unique = true)
    private String ticketCode;

    @Column(name = "totalPrice", nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "description")
    private String description;

    @Column(name = "feePayer", nullable = false)
    private String feePayer;

    @Column(name = "codAmount", nullable = false)
    private BigDecimal codAmount;

    @Column(name = "pickupStopId", nullable = false)
    private int pickupStopId;

    @Column(name = "dropoffStopId", nullable = false)
    private int dropoffStopId;

    @Column(name = "status", nullable = false)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "soldBy")
    private Staff soldBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loadedBy")
    private Staff loadedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unloadedBy")
    private Staff unloadedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deliveredBy")
    private Staff deliveredBy;

    @OneToOne(mappedBy = "cargoTicket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payment payment;

    @OneToMany(mappedBy = "cargoTicket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CargoTicketDetail> cargoTicketDetails;
}
