const PHONE_PATTERN = /^0(3|5|7|8|9)[0-9]{8}$/;
const EMAIL_PATTERN = /^[\w.+-]+@[\w.-]+\.[A-Za-z]{2,}$/;
const FULL_NAME_PATTERN = /^[\p{L}][\p{L}\s'.\-]{1,99}$/u;

/** Aligned with passenger booking + AccompaniedChildDTO (0–6 years old). */
export const getChildBirthYearMax = () => new Date().getFullYear();
export const getChildBirthYearMin = () => getChildBirthYearMax() - 6;

export function buildPassengerInitialForm(seat) {
    const hasChild = Boolean(seat?.childFullname);
    return {
        fullName: seat?.fullName || '',
        phone: seat?.phone || '',
        email: seat?.email || '',
        hasChild,
        childFullname: seat?.childFullname || '',
        childBirthYear: seat?.childBirthYear ? String(seat.childBirthYear) : '',
    };
}

export function validatePassengerForm(form) {
    const errors = {};

    if (!form.fullName.trim()) {
        errors.fullName = 'Vui lòng nhập họ tên.';
    } else if (!FULL_NAME_PATTERN.test(form.fullName.trim())) {
        errors.fullName = 'Họ tên không hợp lệ.';
    }

    if (!form.phone.trim()) {
        errors.phone = 'Vui lòng nhập số điện thoại.';
    } else if (!PHONE_PATTERN.test(form.phone.trim())) {
        errors.phone = 'Số điện thoại không hợp lệ.';
    }

    if (!form.email.trim()) {
        errors.email = 'Vui lòng nhập email.';
    } else if (!EMAIL_PATTERN.test(form.email.trim())) {
        errors.email = 'Email không hợp lệ.';
    }

    if (form.hasChild) {
        if (!form.childFullname.trim()) {
            errors.childFullname = 'Vui lòng nhập tên bé.';
        } else if (!FULL_NAME_PATTERN.test(form.childFullname.trim())) {
            errors.childFullname = 'Họ tên bé không hợp lệ.';
        }

        const birthYear = Number(form.childBirthYear);
        const minBirthYear = getChildBirthYearMin();
        const maxBirthYear = getChildBirthYearMax();
        if (!form.childBirthYear) {
            errors.childBirthYear = 'Vui lòng nhập năm sinh của bé.';
        } else if (!Number.isInteger(birthYear) || birthYear < minBirthYear || birthYear > maxBirthYear) {
            errors.childBirthYear = 'Trẻ em đi kèm phải từ 0 đến 6 tuổi.';
        }
    }

    return errors;
}

export function buildPassengerPayload(form, seat) {
    const payload = {
        fullName: form.fullName.trim(),
        phone: form.phone.trim(),
        email: form.email.trim(),
    };

    const hadChild = Boolean(seat?.childFullname);

    if (form.hasChild) {
        payload.accompaniedChild = {
            fullname: form.childFullname.trim(),
            birthYear: Number(form.childBirthYear),
        };
    } else if (hadChild) {
        payload.removeAccompaniedChild = true;
    }

    return payload;
}

export function passengerFormHasChanges(form, seat) {
    if (!seat) return false;

    const payload = buildPassengerPayload(form, seat);
    const samePassenger = payload.fullName === String(seat.fullName || '').trim()
        && payload.phone === String(seat.phone || '').trim()
        && payload.email === String(seat.email || '').trim();

    if (!samePassenger) return true;
    if (payload.removeAccompaniedChild) return true;

    if (payload.accompaniedChild) {
        return payload.accompaniedChild.fullname !== String(seat.childFullname || '').trim()
            || payload.accompaniedChild.birthYear !== Number(seat.childBirthYear);
    }

    return false;
}
