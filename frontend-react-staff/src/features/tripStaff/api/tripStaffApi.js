import axiosClient from '../../../api/axiosClient';

const BASE = '/v1/staff/trips';

export const tripStaffApi = {
    getAssignedTrips: (date) => axiosClient.get(BASE, { params: { date } }),

    getDashboard: (tripId) => axiosClient.get(`${BASE}/${tripId}/passengers/dashboard`),

    checkInByQr: (tripId, qrToken) =>
        axiosClient.post(`${BASE}/${tripId}/passengers/check-in/qr`, { qrToken }),

    checkInManual: (tripId, ticketDetailId) =>
        axiosClient.post(`${BASE}/${tripId}/passengers/${ticketDetailId}/check-in`),

    getCargoList: (tripId) => axiosClient.get(`${BASE}/${tripId}/cargo`),

    loadCargo: (tripId, cargoTicketId) =>
        axiosClient.post(`${BASE}/${tripId}/cargo/${cargoTicketId}/load`),

    unloadCargo: (tripId, cargoTicketId) =>
        axiosClient.post(`${BASE}/${tripId}/cargo/${cargoTicketId}/unload`),

    deliverCargo: (tripId, cargoTicketId) =>
        axiosClient.post(`${BASE}/${tripId}/cargo/${cargoTicketId}/deliver`),

    startTrip: (tripId) => axiosClient.post(`${BASE}/${tripId}/start`),

    endTrip: (tripId) => axiosClient.post(`${BASE}/${tripId}/end`),

    markNoShow: (tripId, ticketDetailId) =>
        axiosClient.post(`${BASE}/${tripId}/passengers/${ticketDetailId}/no-show`),
};
