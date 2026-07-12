import axiosClient from '../../../api/axiosClient';

const BASE = '/v1/admin/customers';

const customerApi = {
    filterCustomers: (params) => axiosClient.get(`${BASE}`, { params }),
    getCustomerDetail: (id) => axiosClient.get(`${BASE}/${id}`),
    createCustomer: (data) => axiosClient.post(`${BASE}`, data),
    updateCustomer: (id, data) => axiosClient.put(`${BASE}/${id}`, data),
    toggleActive: (id) => axiosClient.patch(`${BASE}/${id}/toggle-active`),
    deleteCustomer: (id) => axiosClient.delete(`${BASE}/${id}`),
};

export default customerApi;
