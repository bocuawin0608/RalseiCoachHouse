package com.ralsei.model.enums;

import lombok.Getter;

@Getter
/**
 * Provides the trip seat status component for the application.
 */
public enum TripSeatStatus {
    AVAILABLE("Còn chỗ"),
    LOCKED("Đang tạm khóa"),
    SOLD("Hết chỗ");

    private final String description;

    TripSeatStatus(String description) {
        this.description = description;
    }
}
