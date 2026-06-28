export const BOOKING_VALIDATION = {
    FULL_NAME_REGEX: /^[\p{L}][\p{L}\s'.-]{1,99}$/u,
    PHONE_REGEX: /^0(3|5|7|8|9)[0-9]{8}$/,
    EMAIL_REGEX: /^$|^[\\w.+-]+@[\\w.-]+\\.[A-Za-z]{2,}$/
};

export const getChildBirthYearMax = () => new Date().getFullYear();
export const getChildBirthYearMin = () => new Date().getFullYear() - 12;

export const bookingValidationRules = {
    fullname: {
        required: 'Vui lòng nhập họ tên',
        pattern: {
            value: BOOKING_VALIDATION.FULL_NAME_REGEX,
            message: 'Họ tên không hợp lệ. Vui lòng nhập ít nhất 2 ký tự, chỉ gồm chữ cái và khoảng trắng!',
        },
    },
    phone: {
        required: 'Vui lòng nhập số điện thoại',
        pattern: {
            value: BOOKING_VALIDATION.PHONE_REGEX,
            message: 'SĐT không hợp lệ. Vui lòng nhập 10 chữ số, bắt đầu bằng 03 hoặc 05 hoặc 07 hoặc 08 hoặc 09!',
        },
    },
    email: {
        pattern: {
            value: BOOKING_VALIDATION.EMAIL_REGEX,
            message: 'Email không hợp lệ!',
        },
    },
    childName: {
        required: 'Vui lòng nhập tên bé',
        pattern: {
            value: BOOKING_VALIDATION.FULL_NAME_REGEX,
            message: 'Họ tên bé không hợp lệ!',
        },
    },
    childBirthYear: {
        required: 'Nhập năm sinh',
        min: {
            value: getChildBirthYearMin(),
            message: 'Năm sinh của trẻ phải >= (năm hiện tại - 12)!',
        },
        max: {
            value: getChildBirthYearMax(),
            message: 'Năm sinh không được > năm hiện tại!',
        },
    },
};
