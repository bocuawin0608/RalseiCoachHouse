const STORAGE_PREFIX = 'booking-payment:';
const ACTIVE_TRIP_PAYMENT_PREFIX = 'booking-active-payment-trip:';

export const savePaymentSession = (transactionId, data) => {
    if (!transactionId || !data) return;
    sessionStorage.setItem(`${STORAGE_PREFIX}${transactionId}`, JSON.stringify(data));
    if (data.tripId) {
        sessionStorage.setItem(`${ACTIVE_TRIP_PAYMENT_PREFIX}${data.tripId}`, transactionId);
    }
};

export const loadPaymentSession = (transactionId) => {
    if (!transactionId) return null;
    const raw = sessionStorage.getItem(`${STORAGE_PREFIX}${transactionId}`);
    if (!raw) return null;
    try {
        return JSON.parse(raw);
    } catch {
        return null;
    }
};

export const loadActivePaymentSessionByTrip = (tripId) => {
    if (!tripId) return null;
    const transactionId = sessionStorage.getItem(`${ACTIVE_TRIP_PAYMENT_PREFIX}${tripId}`);
    if (!transactionId) return null;
    return loadPaymentSession(transactionId);
};

export const clearActivePaymentSessionByTrip = (tripId) => {
    if (!tripId) return;
    sessionStorage.removeItem(`${ACTIVE_TRIP_PAYMENT_PREFIX}${tripId}`);
};

export const mapPaymentPageResponse = (response, existing = {}) => ({
    ticketCode: response.ticketCode,
    transactionId: response.transactionId,
    amount: response.amount,
    bankAccountNumber: response.bankAccountNumber,
    bankName: response.bankName,
    paymentExpiresAt: response.paymentExpiresAt,
    status: response.paymentStatus || response.status || 'PENDING',
    primaryPassengerName: response.primaryPassengerName,
    primaryPassengerPhone: response.primaryPassengerPhone,
    seatCodes: response.seatCodes || [],
    tripId: response.tripId,
    tripTitle: existing.tripTitle,
    tripDate: existing.tripDate,
    cancelToken: existing.cancelToken,
});

export const mapConfirmResponse = (response, summary = {}) => ({
    ticketCode: response.ticketCode,
    transactionId: response.transactionId,
    amount: response.amount,
    bankAccountNumber: response.bankAccountNumber,
    bankName: response.bankName,
    paymentExpiresAt: response.paymentExpiresAt,
    cancelToken: response.cancelToken,
    status: 'PENDING',
    primaryPassengerName: summary.primaryPassengerName,
    primaryPassengerPhone: summary.primaryPassengerPhone,
    seatCodes: summary.seatCodes || [],
    tripId: summary.tripId,
    tripTitle: summary.tripTitle,
    tripDate: summary.tripDate,
});
