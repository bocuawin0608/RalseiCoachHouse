import axiosClient from '../../../api/axiosClient';

const BASE = '/v1/admin/accounts';

const accountApi = {
    filterAccounts: (params) => axiosClient.get(`${BASE}`, { params }),
    getAccountDetail: (id) => axiosClient.get(`${BASE}/${id}`),
    createAccount: (data) => axiosClient.post(`${BASE}`, data),
    updateAccount: (id, data) => axiosClient.put(`${BASE}/${id}`, data),
    assignRoles: (id, data) => axiosClient.put(`${BASE}/${id}/roles`, data),
    resetPassword: (id, data) => axiosClient.patch(`${BASE}/${id}/reset-password`, data),
    toggleActive: (id) => axiosClient.patch(`${BASE}/${id}/toggle-active`),
    deleteAccount: (id) => axiosClient.delete(`${BASE}/${id}`),
    getAllRoles: () => axiosClient.get(`${BASE}/roles`),
};

export default accountApi;
