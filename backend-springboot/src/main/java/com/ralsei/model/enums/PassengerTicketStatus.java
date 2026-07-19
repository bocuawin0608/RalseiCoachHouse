package com.ralsei.model.enums;

import java.util.List;

import lombok.Getter;

@Getter
/**
 * Provides the passenger ticket status component for the application.
 */
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
    /**
     * Executes the parse search value operation.
     *
     * @param value the value supplied for this operation
     *
     * @return the operation result
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

    /**
     * Parses multi-status search filter. Blank/empty input means no status filter.
     */
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
