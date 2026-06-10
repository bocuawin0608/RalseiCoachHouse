package com.ralsei.model.enums;

import lombok.Getter;

@Getter
public enum CoachStatus {
    ACTIVE("Đang hoạt động"),
    MAINTENANCE("Đang bảo trì"),
    RETIRED("Ngừng hoạt động");

    private final String description;

    CoachStatus(String description) {
        this.description = description;
    }
}
