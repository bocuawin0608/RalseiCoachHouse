package com.ralsei.dto.response.cargoticket;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargoTicketResponse {
    private int cargoTicketId;
    private Integer tripId;
    private Integer customerId;
    private String senderName;
    private String senderPhone;
    private String senderEmail;
    private String receiverName;
    private String receiverPhone;
    private String receiverEmail;
    private String ticketCode;
    private BigDecimal totalPrice;
    private String description;
    private String feePayer;
    private BigDecimal codAmount;
    private int pickupStopId;
    private int dropoffStopId;
    private String status;
    private int soldBy;
    private Integer loadedBy;
    private Integer unloadedBy;
    private Integer deliveredBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
