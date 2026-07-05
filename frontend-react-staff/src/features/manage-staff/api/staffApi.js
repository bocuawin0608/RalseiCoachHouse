import axiosClient from '../../../api/axiosClient';

const BASE = '/v1/admin/staff';

const staffApi = {
    filter: (params) => axiosClient.get(`${BASE}`, { params }),
    getDetail: (id) => axiosClient.get(`${BASE}/${id}`),
    update: (id, data) => axiosClient.put(`${BASE}/${id}`, data),
    toggleActive: (id) => axiosClient.patch(`${BASE}/${id}/toggle-active`),
    remove: (id) => axiosClient.delete(`${BASE}/${id}`),
    onboard: (data) => axiosClient.post(`${BASE}/onboard`, data),
};

export default staffApi;
