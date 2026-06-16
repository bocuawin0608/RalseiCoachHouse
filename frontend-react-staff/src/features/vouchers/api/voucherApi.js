import axiosClient from '../../../api/axiosClient';

const BASE = '/v1/vouchers';

export const voucherApi = {
    getAll: (params) => axiosClient.get(BASE, { params }),
    getById: (id) => axiosClient.get(`${BASE}/${id}`),
    create: (data) => axiosClient.post(BASE, data),
    update: (id, data) => axiosClient.put(`${BASE}/${id}`, data),
    delete: (id) => axiosClient.delete(`${BASE}/${id}`),
    getMetrics: () => axiosClient.get(`${BASE}/metrics`),
};
