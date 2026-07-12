import axiosClient from '../../../api/axiosClient';

const BASE = '/v1/admin/roles';

const roleApi = {
    filterRoles: (params) => axiosClient.get(`${BASE}`, { params }),
    getRoleDetail: (id) => axiosClient.get(`${BASE}/${id}`),
    createRole: (data) => axiosClient.post(`${BASE}`, data),
    updateRole: (id, data) => axiosClient.put(`${BASE}/${id}`, data),
    toggleActive: (id) => axiosClient.patch(`${BASE}/${id}/toggle-active`),
    deleteRole: (id) => axiosClient.delete(`${BASE}/${id}`),
};

export default roleApi;
