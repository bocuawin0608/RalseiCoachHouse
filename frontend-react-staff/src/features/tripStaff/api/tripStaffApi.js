import axiosClient from '../../../api/axiosClient';

const BASE = '/v1/staff/trips';

export const tripStaffApi = {
    getAssignedTrips: (date) => axiosClient.get(BASE, { params: { date } }),

    getDashboard: (tripId) => axiosClient.get(`${BASE}/${tripId}/passengers/dashboard`),

    checkInByQr: (tripId, qrToken) =>
        axiosClient.post(`${BASE}/${tripId}/passengers/check-in/qr`, { qrToken }),

    checkInManual: (tripId, ticketDetailId) =>
        axiosClient.post(`${BASE}/${tripId}/passengers/${ticketDetailId}/check-in`),
};
