package com.ralsei.dto.response.cargoticket;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * Unassigned waiting order that can be attached to a selected trip.
 */
public class CargoAssignableTicketResponse {
    private int cargoTicketId;
    private String ticketCode;
    private String senderName;
    private String senderPhone;
    private String receiverName;
    private String receiverPhone;
    private BigDecimal totalPrice;
    private int pickupStopId;
    private String pickupStopName;
    private int dropoffStopId;
    private String dropoffStopName;
    private String status;
    private String feePayer;
    private String paymentMethod;
    private String paymentStatus;
    /** Order volume (dimensionVol × quantity) used for capacity planning. */
    private BigDecimal occupiedVolume;
}
