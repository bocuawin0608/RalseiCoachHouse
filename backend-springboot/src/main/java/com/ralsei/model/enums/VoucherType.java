package com.ralsei.model.enums;

import lombok.Getter;

@Getter
/**
 * Provides the voucher type component for the application.
 */
public enum VoucherType {
    FIXED("FIXED"),
    PERCENT("PERCENT");

    private final String value;

    private VoucherType(String value) {
        this.value = value;
    }
}
