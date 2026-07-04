import axiosClient from '../../../api/axiosClient';
const BASE = '/v1/route-stops';
const GOONG_BASE = '/v2/goong';

export const routeStopApi = {
    createRouteStop: (data) => { return axiosClient.post(BASE, data); },
    updateRouteStop: (id, data) => { return axiosClient.put(`${BASE}/${id}`, data); },
    bulkUpdateOrders: (data) => { return axiosClient.put(`${BASE}/bulk-update-orders`, data); },
    deleteRouteStop: (id) => { return axiosClient.delete(`${BASE}/${id}`); },
    calculateDistances: (data) => { return axiosClient.post(`${GOONG_BASE}/calculate-route-distances`, data); }
};
