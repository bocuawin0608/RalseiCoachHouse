package com.ralsei.util;

public final class PhoneNumberUtility {

    private PhoneNumberUtility() {
    }

    public static String normalizeToLocalFormat(String phone) {
        if (phone == null) {
            return null;
        }

        String trimmed = phone.trim();
        if (trimmed.startsWith("+84")) {
            return "0" + trimmed.substring(3);
        }
        if (trimmed.startsWith("84") && trimmed.length() == 11) {
            return "0" + trimmed.substring(2);
        }
        return trimmed;
    }

    public static boolean matchesLocalPhone(String expectedLocalPhone, String actualPhone) {
        String normalizedExpected = normalizeToLocalFormat(expectedLocalPhone);
        String normalizedActual = normalizeToLocalFormat(actualPhone);
        return normalizedExpected != null
                && normalizedActual != null
                && normalizedExpected.equals(normalizedActual);
    }
}
