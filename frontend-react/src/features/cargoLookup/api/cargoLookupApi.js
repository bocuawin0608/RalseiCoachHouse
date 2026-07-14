import axiosClient from '../../../api/axiosClient';

/** Read-only API boundary for authenticated cargo history. */
export const cargoLookupApi = {
    /** Loads orders owned by the account identified by the access token. */
    getHistory: () => axiosClient.get('/v1/customer/cargo-history'),
    /** Disables cargo ticket using the staff endpoint */
    disableCargoOrder: (id) => axiosClient.put(`/v1/ticket-staff/cargo-tickets/${id}/disable`),
};
