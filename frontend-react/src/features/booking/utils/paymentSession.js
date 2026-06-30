const STORAGE_PREFIX = 'booking-payment:';

export const savePaymentSession = (transactionId, data) => {
    if (!transactionId || !data) return;
    sessionStorage.setItem(`${STORAGE_PREFIX}${transactionId}`, JSON.stringify(data));
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

export const mapPaymentPageResponse = (response) => ({
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
});

export const mapConfirmResponse = (response, summary = {}) => ({
    ticketCode: response.ticketCode,
    transactionId: response.transactionId,
    amount: response.amount,
    bankAccountNumber: response.bankAccountNumber,
    bankName: response.bankName,
    paymentExpiresAt: response.paymentExpiresAt,
    status: 'PENDING',
    primaryPassengerName: summary.primaryPassengerName,
    primaryPassengerPhone: summary.primaryPassengerPhone,
    seatCodes: summary.seatCodes || [],
    tripId: summary.tripId,
});
