package com.ralsei.model.enums;

import lombok.Getter;

@Getter
public enum PassengerTicketDetailStatus {
    PENDING("Đang xử lý"),
    CONFIRMED("Đã xác nhận"),
    CHECKED_IN("Đã check-in"),
    CANCELLED("Đã hủy"),
    EXPIRED("Đã hết hiệu lực");

    private final String message;

    private PassengerTicketDetailStatus(String message) {
        this.message = message;
    }
}
