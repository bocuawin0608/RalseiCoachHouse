package com.ralsei.staff.model.enums;

import lombok.Getter;

/**
 * Provides the trip seat status component for the application.
 */
@Getter

public enum TripSeatStatus {
    AVAILABLE("Còn chỗ"),
    LOCKED("Đang tạm khóa"),
    SOLD("Hết chỗ");

    private final String description;

    TripSeatStatus(String description) {
        this.description = description;
    }
}
