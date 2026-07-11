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

    /**
     * Parses a ticket status filter from search input.
     *
     * @param value raw status text, may be {@code null}
     * @return matching enum constant, or {@code null} when input is blank
     * @throws IllegalArgumentException when the value is not a supported status
     */
    public static PassengerTicketStatus parseSearchValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return PassengerTicketStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Trạng thái vé không hợp lệ.");
        }
    }

}
