import axiosClient from '../../../api/axiosClient';

/**
 * Provides the only frontend boundary to authenticated booking-history endpoints.
 */
export const customerHistoryApi = {
    /** Returns every booking owned by the signed-in customer. */
    getHistory: () => axiosClient.get('/v1/customer/history'),

    /** Returns one customer-owned booking selected by public ticket code. */
    getDetail: (ticketCode) => axiosClient.get(
        `/v1/customer/history/${encodeURIComponent(ticketCode)}`,
    ),

    /** Returns a protected PNG blob without exposing the persisted QR token. */
    getSeatQr: (ticketDetailId) => axiosClient.get(
        `/v1/customer/history/seats/${ticketDetailId}/qr`,
        { responseType: 'blob' },
    ),

    /** Cancels an owned future ticket and creates a pending bank refund request. */
    cancelTicket: (ticketCode, bankDestination) => axiosClient.post(
        `/v1/customer/history/${encodeURIComponent(ticketCode)}/cancel`,
        bankDestination,
    ),
};
