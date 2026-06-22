package com.ralsei.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class CargoTrackingResponse {
    private String ticketCode;
    private String status;
    private String senderName;
    private String senderPhone;
    private String receiverName;
    private String receiverPhone;
    private String pickupStopName;
    private String dropoffStopName;
    private BigDecimal totalPrice;
    private String feePayer;
    private BigDecimal codAmount;
    private String description;
    private String tripRouteName;
    private LocalDateTime tripDepartureTime;
    private List<CargoDetailItem> items;

    @Data
    @Builder
    @AllArgsConstructor
    public static class CargoDetailItem {
        private String description;
        private int quantity;
        private BigDecimal weightKg;
        private BigDecimal dimensionVol;
        private BigDecimal calculatedPrice;
        private String unit;
    }
}
