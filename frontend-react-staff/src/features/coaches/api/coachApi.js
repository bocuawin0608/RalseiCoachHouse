import axiosClient from '../../../api/axiosClient';
const BASE = '/v1/coaches';

export const coachApi = {
    filterCoaches: (params) => axiosClient.get(BASE, { params }),

    createCoach: (data) => axiosClient.post(BASE, data),

    getCoachDetail: (id) => axiosClient.get(`${BASE}/${id}`),

    updateCoachInfo: (id, data) => axiosClient.put(`${BASE}/${id}`, data),

    updateCoachSeats: (id, data) => axiosClient.patch(`${BASE}/${id}/seats`, data),

    getStatusChangeCheck: (id, target) =>
        axiosClient.get(`${BASE}/${id}/status-change-check`, { params: { target } }),

    reportMaintenance: (id, data) => axiosClient.post(`${BASE}/${id}/report-maintenance`, data),

    reactivate: (id, data) => axiosClient.post(`${BASE}/${id}/reactivate`, data),

    retire: (id, data) => axiosClient.post(`${BASE}/${id}/retire`, data),

    getStatusLogs: (id, params) => axiosClient.get(`${BASE}/${id}/status-logs`, { params }),
};
