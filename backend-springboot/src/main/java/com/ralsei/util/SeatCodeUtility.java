package com.ralsei.util;

import java.util.regex.Pattern;

import com.ralsei.util.validation.CoachValidationPatterns;

public final class SeatCodeUtility {

    private static final Pattern SEAT_CODE_PATTERN = Pattern.compile(CoachValidationPatterns.SEAT_CODE);

    private SeatCodeUtility() {}

    /**
     * Returns whether the valid is active.
     *
     * @param input the value supplied for this operation
     *
     * @return {@code true} if the valid is active; otherwise {@code false}
     */
    public static boolean isValid(String input) {
        String normalized = normalizeOrNull(input);
        return normalized != null && SEAT_CODE_PATTERN.matcher(normalized).matches();
    }

    /**
     * Executes the normalize operation.
     *
     * @param input the value supplied for this operation
     *
     * @return the operation result
     */
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
