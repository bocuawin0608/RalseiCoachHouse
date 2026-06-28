package com.ralsei.util.validation;

public final class BookingValidationPatterns {

    public static final String FULL_NAME =
            "^[\\p{L}][\\p{L}\\s'.\\-]{1,99}$";

    public static final String PHONE =
            "^0(3|5|7|8|9)[0-9]{8}$";

    public static final String EMAIL =
            "^$|^[\\w.+-]+@[\\w.-]+\\.[A-Za-z]{2,}$";

    private BookingValidationPatterns() {}
}
