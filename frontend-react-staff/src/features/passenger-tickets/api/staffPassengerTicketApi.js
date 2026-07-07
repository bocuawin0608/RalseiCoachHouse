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

    getTripSeatMap(tripId) {
        return axiosClient.get(`/v1/staff/passenger-tickets/trips/${tripId}/seat-map`);
    },

    lockSeat(tripId, tripSeatId, holdToken) {
        return axiosClient.post(
            `/v1/staff/passenger-tickets/trips/${tripId}/seats/lock`,
            { tripSeatIds: [tripSeatId] },
            { headers: { 'X-Staff-Seat-Session': holdToken } }
        );
    },

    releaseSeats(tripId, tripSeatIds, holdToken) {
        return axiosClient.post(
            `/v1/staff/passenger-tickets/trips/${tripId}/seats/release`,
            { tripSeatIds },
            { headers: { 'X-Staff-Seat-Session': holdToken } }
        );
    },

    changeSeat(ticketCode, ticketDetailId, newTripSeatId, holdToken) {
        return axiosClient.patch(
            `/v1/staff/passenger-tickets/${encodeURIComponent(ticketCode)}/details/${ticketDetailId}/seat`,
            { newTripSeatId },
            { headers: { 'X-Staff-Seat-Session': holdToken } }
        );
    },
};
