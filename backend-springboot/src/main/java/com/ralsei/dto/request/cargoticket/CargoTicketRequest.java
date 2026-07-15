package com.ralsei.dto.request.cargoticket;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.ralsei.model.Staff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * Represents the request payload for cargo ticket operations.
 */
public class CargoTicketRequest {
    @Min(value = 1, message = "Trip ID must be greater than 0")
    private Integer tripId;

    @Min(value = 1, message = "Customer ID must be greater than 0")
    private Integer customerId;

    @NotBlank(message = "Sender name is required")
    @Size(max = 100, message = "Sender name must not exceed 100 characters")
    private String senderName;

    @NotBlank(message = "Sender phone is required")
    @Size(max = 20, message = "Sender phone must not exceed 20 characters")
    private String senderPhone;

    @NotBlank(message = "Receiver name is required")
    @Size(max = 100, message = "Receiver name must not exceed 100 characters")
    private String receiverName;

    @NotBlank(message = "Receiver phone is required")
    @Size(max = 20, message = "Receiver phone must not exceed 20 characters")
    private String receiverPhone;

    @Size(max = 50, message = "Ticket code must not exceed 50 characters")
    private String ticketCode;

    @NotNull(message = "Total price is required")
    @DecimalMin(value = "0.00", message = "Total price must not be negative")
    private BigDecimal totalPrice;

    private String description;

    @NotBlank(message = "Fee payer is required")
    @Pattern(regexp = "SENDER|RECEIVER", message = "Fee payer must be SENDER or RECEIVER")
    private String feePayer;

    @NotNull(message = "COD amount is required")
    @DecimalMin(value = "0.00", message = "COD amount must not be negative")
    private BigDecimal codAmount;

    @Min(value = 1, message = "Pickup stop ID must be greater than 0")
    private int pickupStopId;

    @Min(value = 1, message = "Drop-off stop ID must be greater than 0")
    private int dropoffStopId;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "RECEIVED|LOADED|ARRIVED|DELIVERED|CANCELLED|REJECTED|ABANDONED", message = "Cargo ticket status is invalid")
    private String status;

    @NotNull(message = "Seller is required")
    private Staff soldBy;

    private Staff loadedBy;
    private Staff unloadedBy;
    private Staff deliveredBy;

    @NotBlank(message = "Payment method is required")
    @Pattern(regexp = "CASH|BANK_TRANSFER", message = "Payment method must be CASH or BANK_TRANSFER")
    private String paymentMethod;
}
