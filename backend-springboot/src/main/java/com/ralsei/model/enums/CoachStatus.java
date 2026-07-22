package com.ralsei.model.enums;

import lombok.Getter;

@Getter
/**
 * Provides the coach status component for the application.
 */
public enum CoachStatus {
    ACTIVE("Đang hoạt động"),
    HAVE_INCIDENT("Gặp sự cố không thể khắc phục"),
    MAINTENANCE("Đang bảo trì"),
    RETIRED("Ngừng hoạt động");

    private final String description;

    CoachStatus(String description) {
        this.description = description;
    }
}
