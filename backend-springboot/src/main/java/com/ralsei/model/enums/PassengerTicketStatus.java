package com.ralsei.model.enums;

import lombok.Getter;

@Getter
public enum PassengerTicketStatus {
    PENDING("Đang xử lý"),
    CONFIRMED("Đã xác nhận"),
    CHANGED("Có thay đổi sau xác nhận"),
    CANCELLED("Đã hủy");

    private final String message;

    private PassengerTicketStatus(String message) {
        this.message = message;
    }

}
