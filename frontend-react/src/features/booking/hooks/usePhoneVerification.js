import { useCallback, useState } from 'react';
import { bookingApi } from '../api/bookingApi';
import { BOOKING_VALIDATION } from '../utils/bookingValidation';

export function usePhoneVerification() {
    const [verifiedPhones, setVerifiedPhones] = useState({});
    const [phoneCheckLoading, setPhoneCheckLoading] = useState(null);
    const [phoneCheckError, setPhoneCheckError] = useState('');
    const [otpPhone, setOtpPhone] = useState(null);

    const markKnownPhone = useCallback((phone) => {
        const trimmed = phone?.trim();
        if (!trimmed) return;

        setVerifiedPhones((prev) => ({
            ...prev,
            [trimmed]: { isKnown: true },
        }));
    }, []);

    const clearPhoneVerification = useCallback((phone) => {
        const trimmed = phone?.trim();
        if (!trimmed) return;

        setVerifiedPhones((prev) => {
            if(!prev[trimmed]) return prev;

            const next = { ...prev };
            delete next[trimmed];
            return next;
        });
    }, []);

    const isPhoneVerified = useCallback((phone) => {
        const trimmed = phone?.trim();
        return Boolean(trimmed && verifiedPhones[trimmed]);
    }, [verifiedPhones]);

    const getUnverifiedPhones = useCallback((passengers = []) => {
        const uniquePhones = [...new Set(
            passengers
                .map((passenger) => passenger.phone?.trim())
                .filter(Boolean)
        )];

        return uniquePhones.filter((phone) => !verifiedPhones[phone]);
    }, [verifiedPhones]);

    const handleOtpVerified = useCallback((phone, idToken) => {
        const trimmed = phone?.trim();
        if (!trimmed) return;

        setVerifiedPhones((prev) => ({
            ...prev,
            [trimmed]: { isKnown: false, idToken },
        }));
        setOtpPhone(null);
        setPhoneCheckError('');
    }, []);

    const closeOtpModal = useCallback(() => {
        setOtpPhone(null);
    }, []);

    const checkPhoneOnBlur = useCallback(async (phone, passengerIndex, setValue, getValues) => {
        const trimmed = phone?.trim() ?? '';
        if (trimmed !== phone) {
            setValue(`passengers.${passengerIndex}.phone`, trimmed, { shouldValidate: true });
        }
        if (!trimmed || !BOOKING_VALIDATION.PHONE_REGEX.test(trimmed)) {
            return;
        }

        if (isPhoneVerified(trimmed)) {
            return;
        }

        setPhoneCheckLoading(trimmed);
        setPhoneCheckError('');

        try {
            const result = await bookingApi.checkPhone(trimmed);

            if (result.isKnown) {
                markKnownPhone(trimmed);

                if (result.suggestedProfile) {
                    const currentFullname = getValues(`passengers.${passengerIndex}.fullname`);
                    const currentEmail = getValues(`passengers.${passengerIndex}.email`);

                    if (!currentFullname?.trim() && result.suggestedProfile.fullname) {
                        setValue(`passengers.${passengerIndex}.fullname`, result.suggestedProfile.fullname);
                    }
                    if (!currentEmail?.trim() && result.suggestedProfile.email) {
                        setValue(`passengers.${passengerIndex}.email`, result.suggestedProfile.email);
                    }
                }
                return;
            }

            setOtpPhone(trimmed);
        } catch (err) {
            setPhoneCheckError(err.response?.data?.message || 'Không thể kiểm tra số điện thoại. Vui lòng thử lại!');
        } finally {
            setPhoneCheckLoading(null);
        }
    }, [isPhoneVerified, markKnownPhone]);

    const openOtpForPhone = useCallback((phone) => {
        const trimmed = phone?.trim();
        if (!trimmed || !BOOKING_VALIDATION.PHONE_REGEX.test(trimmed)) {
            return;
        }
        setOtpPhone(trimmed);
    }, []);

    const countPhoneUsage = (passengers, phone, excludeIndex) =>
        passengers.filter((p, i) => i !== excludeIndex && p.phone?.trim() === phone).length;

    return {
        verifiedPhones,
        phoneCheckLoading,
        phoneCheckError,
        otpPhone,
        markKnownPhone,
        clearPhoneVerification,
        isPhoneVerified,
        getUnverifiedPhones,
        handleOtpVerified,
        closeOtpModal,
        checkPhoneOnBlur,
        openOtpForPhone,
        countPhoneUsage
    };
}
