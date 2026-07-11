package com.ralsei.util;

/**
 * Lightweight string normalization for request DTOs.
 * Trim runs in record compact constructors so Bean Validation sees cleaned values.
 */
public final class StringNormalize {

    private StringNormalize() {}

    /**
     * Trims whitespace; returns null when the input is null or blank after trim.
     */
    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Trims whitespace; returns empty string when the input is null.
     */
    public static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
