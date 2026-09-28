package com.ralsei.customer.model.enums;

import java.util.List;

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

    public static List<String> parseSearchValues(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
            .filter(value -> value != null && !value.isBlank())
            .map(PassengerTicketStatus::parseSearchValue)
            .map(PassengerTicketStatus::name)
            .distinct()
            .toList();
    }
}
