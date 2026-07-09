export const BOOKING_VALIDATION = {
    FULL_NAME_REGEX: /^[\p{L}][\p{L}\s'.-]{1,99}$/u,
    PHONE_REGEX: /^0(3|5|7|8|9)[0-9]{8}$/,
    EMAIL_REGEX: /^[\w.+-]+@[\w.-]+\.[A-Za-z]{2,}$/
};

export const EMAIL_MAX_LENGTH = 254; // RFC 5321

export const trimInput = (value) => (typeof value === 'string' ? value.trim() : value);

export const getChildBirthYearMax = () => new Date().getFullYear();
export const getChildBirthYearMin = () => new Date().getFullYear() - 6;

export const validateChildBirthYearValue = (val) => {
    const y = Number(val);
    if (Number.isNaN(y)) return 'Năm sinh không hợp lệ!';
    const curr = new Date().getFullYear();
    return (y >= curr - 6 && y <= curr) || 'Trẻ em đi kèm phải từ 0 đến 6 tuổi.';
};

export const bookingValidationRules = {
    fullname: {
        setValueAs: trimInput,
        required: 'Vui lòng nhập họ tên',
        pattern: {
            value: BOOKING_VALIDATION.FULL_NAME_REGEX,
            message: 'Họ tên không hợp lệ. Vui lòng nhập ít nhất 2 ký tự, chỉ gồm chữ cái và khoảng trắng!',
        },
    },
    phone: {
        setValueAs: trimInput,
        required: 'Vui lòng nhập số điện thoại',
        pattern: {
            value: BOOKING_VALIDATION.PHONE_REGEX,
            message: 'SĐT không hợp lệ. Vui lòng nhập 10 chữ số, bắt đầu bằng 03 hoặc 05 hoặc 07 hoặc 08 hoặc 09!',
        },
    },
    email: {
        setValueAs: trimInput,
        required: 'Vui lòng nhập email',
        maxLength: {
            value: EMAIL_MAX_LENGTH,
            message: 'Email không được vượt quá 254 ký tự.',
        },
        pattern: {
            value: BOOKING_VALIDATION.EMAIL_REGEX,
            message: 'Email không hợp lệ!',
        },
    },
    childName: {
        setValueAs: trimInput,
        required: 'Vui lòng nhập tên bé',
        pattern: {
            value: BOOKING_VALIDATION.FULL_NAME_REGEX,
            message: 'Họ tên bé không hợp lệ!',
        },
    },
    childBirthYear: {
        required: 'Nhập năm sinh',
        validate: validateChildBirthYearValue,
    },
};
