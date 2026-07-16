package com.ralsei.dto.response.cargoticket;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ralsei.model.Staff;
import com.ralsei.model.Payment;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * Represents the response payload for cargo ticket operations.
 */
public class CargoTicketResponse {
    private int cargoTicketId;
    private Integer tripId;
    private Integer customerId;
    private String senderName;
    private String senderPhone;
    private String receiverName;
    private String receiverPhone;
    private String ticketCode;
    private BigDecimal totalPrice;
    private String description;
    private String feePayer;
    private BigDecimal codAmount;
    private int pickupStopId;
    private String pickupStopName;
    private int dropoffStopId;
    private String dropoffStopName;
    private String status;
    /** Route and vehicle identify where this package physically belongs. */
    private String routeName;
    private String licensePlate;
    private String destinationAgencyName;
    /** Driver identity and contact data used when tracing a missing package. */
    private String driverName;
    private String driverPhone;
    private String driverCccd;
    /** Attendant identity and contact data used for load/unload accountability. */
    private String attendantName;
    private String attendantPhone;
    private String attendantCccd;
    private Staff soldBy;
    private Staff loadedBy;
    private Staff unloadedBy;
    private Staff deliveredBy;

    @JsonIgnoreProperties({"cargoTicket"})
    private Payment payment;

    private String qrUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
