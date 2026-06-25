package com.ralsei.model.enums;

import lombok.Getter;

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
