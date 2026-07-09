export const COACH_VALIDATION = {
    LICENSE_PLATE_INPUT_REGEX: /^[1-9][0-9][A-Za-z]{1,2}[- ]?([0-9]{4,5}|[0-9]{3}\.[0-9]{2})$/,
    LICENSE_PLATE_MESSAGE:
        'Biển số xe không đúng định dạng chuẩn ô tô (Ví dụ hợp lệ: 73B55522, 73B-555.22 hoặc 29A-1234).',
    SEAT_CODE_REGEX: /^[A-Za-z]{1,2}[0-9]{2}$/,
    SEAT_CODE_MAX_LENGTH: 4,
    SEAT_CODE_MESSAGE: 'Mã ghế phải gồm 1-2 chữ cái và 2 chữ số (Ví dụ: A01, LX01).',
    YEAR_MIN: 2000,
    getYearMax: () => new Date().getFullYear(),
};

/**
 * Validate và format biển số xe theo quy chuẩn Việt Nam.
 * @returns {{valid: boolean, data: string}}
 */
export const validateAndFormatLicensePlate = (input) => {
    if (!input || !String(input).trim()) {
        return { valid: false, data: input };
    }

    const trimmed = String(input).trim();
    if (!COACH_VALIDATION.LICENSE_PLATE_INPUT_REGEX.test(trimmed)) {
        return { valid: false, data: trimmed };
    }

    const cleanPlate = trimmed.toUpperCase().replace(/[^A-Z0-9]/g, '');
    const match = cleanPlate.match(/^([1-9][0-9])([A-Z]{1,2})([0-9]{4,5})$/);
    if (!match) {
        return { valid: false, data: trimmed };
    }

    const province = match[1];
    const series = match[2];
    let serial = match[3];
    if (serial.length === 5) {
        serial = `${serial.slice(0, 3)}.${serial.slice(3)}`;
    }

    return { valid: true, data: `${province}${series}-${serial}` };
};

export const normalizeSeatCodeInput = (input) => {
    if (input == null) return '';
    return String(input).toUpperCase().replace(/[^A-Z0-9]/g, '').slice(0, COACH_VALIDATION.SEAT_CODE_MAX_LENGTH);
};

export const isValidSeatCode = (input) => {
    const normalized = normalizeSeatCodeInput(input);
    return COACH_VALIDATION.SEAT_CODE_REGEX.test(normalized);
};

export const getMinDateTime = () => {
    const now = new Date();
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    return now.toISOString().slice(0, 16);
};

export const isFutureDateTimeLocal = (value) => {
    if (!value) return true;
    return new Date(value).getTime() > Date.now();
};
