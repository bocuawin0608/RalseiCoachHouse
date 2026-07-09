package com.ralsei.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ralsei.util.validation.CoachValidationPatterns;

public final class LicensePlateUtility {

    private static final Pattern INPUT_PATTERN = Pattern.compile(CoachValidationPatterns.LICENSE_PLATE_INPUT);
    private static final Pattern PARSE_PATTERN = Pattern.compile("^([1-9][0-9])([A-Z]{1,2})(\\d{4,5})$");

    private LicensePlateUtility() {}

    public static boolean isValid(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }
        return INPUT_PATTERN.matcher(input.trim()).matches();
    }

    /**
     * Normalizes a valid license plate to canonical DB format: {@code 73B-555.22} or {@code 29A-1234}.
     *
     * @throws IllegalArgumentException when the input is not a valid plate
     */
    public static String normalize(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException(CoachValidationPatterns.LICENSE_PLATE_MESSAGE);
        }

        String trimmed = input.trim();
        if (!INPUT_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(CoachValidationPatterns.LICENSE_PLATE_MESSAGE);
        }

        String compact = trimmed.toUpperCase().replaceAll("[^A-Z0-9]", "");
        Matcher matcher = PARSE_PATTERN.matcher(compact);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(CoachValidationPatterns.LICENSE_PLATE_MESSAGE);
        }

        String province = matcher.group(1);
        String series = matcher.group(2);
        String serial = matcher.group(3);
        if (serial.length() == 5) {
            serial = serial.substring(0, 3) + "." + serial.substring(3);
        }

        return province + series + "-" + serial;
    }
}
