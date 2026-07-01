import axiosClient from '../../../api/axiosClient';

const BASE = '/v1/admin/ticket-agencies';

const ticketAgencyApi = {
    filter: (params) => axiosClient.get(`${BASE}`, { params }),
    getDetail: (id) => axiosClient.get(`${BASE}/${id}`),
    create: (data) => axiosClient.post(`${BASE}`, data),
    update: (id, data) => axiosClient.put(`${BASE}/${id}`, data),
    toggleActive: (id) => axiosClient.patch(`${BASE}/${id}/toggle-active`),
    delete: (id) => axiosClient.delete(`${BASE}/${id}`),
    getCoachStopDropdown: () => axiosClient.get(`${BASE}/coach-stop-dropdown`),
};

export default ticketAgencyApi;
