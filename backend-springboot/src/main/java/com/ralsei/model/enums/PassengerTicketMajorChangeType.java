package com.ralsei.model.enums;

import lombok.Getter;

@Getter
public enum PassengerTicketMajorChangeType {
    TRANSFER_TRIP("Đã chuyển chuyến"),
    CANCEL_PARTIAL("Đã hủy một phần");

    private final String message;

    PassengerTicketMajorChangeType(String message) {
        this.message = message;
    }
}
