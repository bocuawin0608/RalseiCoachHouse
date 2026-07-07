import axiosClient from '../../../api/axiosClient';

export const staffPassengerTicketApi = {
    search(params) {
        return axiosClient.get('/v1/staff/passenger-tickets/search', { params });
    },

    getDetail(ticketCode) {
        return axiosClient.get(`/v1/staff/passenger-tickets/${encodeURIComponent(ticketCode)}`);
    },

    getSeatQrBlob(ticketCode, ticketDetailId) {
        return axiosClient.get(
            `/v1/staff/passenger-tickets/${encodeURIComponent(ticketCode)}/details/${ticketDetailId}/qr`,
            { responseType: 'blob' }
        );
    },

    changePassengerInfo(ticketCode, ticketDetailId, payload) {
        return axiosClient.patch(
            `/v1/staff/passenger-tickets/${encodeURIComponent(ticketCode)}/details/${ticketDetailId}/passenger-info`,
            payload
        );
    },

    cancelFull(ticketCode, payload) {
        return axiosClient.post(
            `/v1/staff/passenger-tickets/${encodeURIComponent(ticketCode)}/cancel`,
            payload
        );
    },
};
