export const MAX_CARGO_SURCHARGE_PRICE = 9999999999999;

/**
 * Keeps the price input as text so large values are not rounded by JavaScript.
 */
export const normalizeCargoPriceInput = (value) => value;

/**
 * Validates the surcharge price against the DECIMAL(15,2) database limit.
 */
export const validateCargoSurchargePrice = (value) => {
    if (value === '') {
        return 'Vui lòng nhập đơn giá!';
    }

    const numericValue = Number(value);

    if (!Number.isFinite(numericValue) || numericValue < 0) {
        return 'Đơn giá phải là số không âm!';
    }

    if (numericValue > MAX_CARGO_SURCHARGE_PRICE) {
        return 'Đơn giá không được vượt quá 9.999.999.999.999!';
    }

    return '';
};
