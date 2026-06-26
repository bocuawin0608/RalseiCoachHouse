package com.ralsei.model.enums;

import lombok.Getter;

@Getter
public enum VoucherType {
    FIXED("FIXED"),
    PERCENT("PERCENT");

    private final String value;

    private VoucherType(String value) {
        this.value = value;
    }
}
