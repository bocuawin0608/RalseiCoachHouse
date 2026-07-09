package com.ralsei.util;

import java.util.regex.Pattern;

import com.ralsei.util.validation.CoachValidationPatterns;

public final class SeatCodeUtility {

    private static final Pattern SEAT_CODE_PATTERN = Pattern.compile(CoachValidationPatterns.SEAT_CODE);

    private SeatCodeUtility() {}

    public static boolean isValid(String input) {
        String normalized = normalizeOrNull(input);
        return normalized != null && SEAT_CODE_PATTERN.matcher(normalized).matches();
    }

    public static String normalize(String input) {
        String normalized = normalizeOrNull(input);
        if (normalized == null || !SEAT_CODE_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(CoachValidationPatterns.SEAT_CODE_MESSAGE);
        }
        return normalized;
    }

    private static String normalizeOrNull(String input) {
        if (input == null) {
            return null;
        }
        String trimmed = input.trim();
        return trimmed.isEmpty() ? null : trimmed.toUpperCase();
    }
}
