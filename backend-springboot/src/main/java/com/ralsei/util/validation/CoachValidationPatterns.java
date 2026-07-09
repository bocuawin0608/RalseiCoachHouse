package com.ralsei.util.validation;

public final class CoachValidationPatterns {

    /** Accepts flexible user input before normalization (case-insensitive letters). */
    public static final String LICENSE_PLATE_INPUT =
            "^[1-9][0-9][A-Za-z]{1,2}[- ]?([0-9]{4,5}|[0-9]{3}\\.[0-9]{2})$";

    public static final String LICENSE_PLATE_MESSAGE =
            "Biển số xe không đúng định dạng chuẩn ô tô (Ví dụ hợp lệ: 73B55522, 73B-555.22 hoặc 29A-1234).";

    public static final String SEAT_CODE = "^[A-Za-z]{1,2}[0-9]{2}$";

    public static final String SEAT_CODE_MESSAGE =
            "Mã ghế phải gồm 1-2 chữ cái và 2 chữ số (Ví dụ: A01, LX01).";

    public static final int YEAR_MIN = 2000;

    private CoachValidationPatterns() {}
}
