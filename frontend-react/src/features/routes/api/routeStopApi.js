import axiosClient from '../../../api/axiosClient';
const BASE = '/v1/route-stops';

export const routeStopApi = {
    createRouteStop: (data) => { return axiosClient.post(BASE, data); },
    updateRouteStop: (id, data) => { return axiosClient.put(`${BASE}/${id}`, data); },
    deleteRouteStop: (id) => { return axiosClient.delete(`${BASE}/${id}`); }
};
