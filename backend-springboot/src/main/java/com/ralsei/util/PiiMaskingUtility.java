package com.ralsei.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PiiMaskingUtility {

    public static String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return "***";
        }

        String trimmed = phone.trim();
        if (trimmed.length() < 7) {
            return "***";
        }

        int visiblePrefix = 3;
        int visibleSuffix = 3;
        return trimmed.substring(0, visiblePrefix)
                + "****"
                + trimmed.substring(trimmed.length() - visibleSuffix);
    }

    public static String maskFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "***";
        }

        String trimmed = fullName.trim();
        int spaceIndex = trimmed.indexOf(' ');
        if (spaceIndex > 0) {
            return trimmed.substring(0, spaceIndex) + " ***";
        }

        if (trimmed.length() <= 3) {
            return "***";
        }

        return trimmed.substring(0, 3) + "***";
    }
}
