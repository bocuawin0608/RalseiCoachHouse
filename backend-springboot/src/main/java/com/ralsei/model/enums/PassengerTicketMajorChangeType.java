package com.ralsei.model.enums;

import lombok.Getter;

@Getter
/**
 * Provides the passenger ticket major change type component for the application.
 */
public enum PassengerTicketMajorChangeType {
    TRANSFER_TRIP("Đã chuyển chuyến"),
    CANCEL_PARTIAL("Đã hủy một phần");

    private final String message;

    PassengerTicketMajorChangeType(String message) {
        this.message = message;
    }
}
