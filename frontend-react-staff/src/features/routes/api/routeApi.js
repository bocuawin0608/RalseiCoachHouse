import axiosClient from '../../../api/axiosClient';
const BASE = '/v1/routes';

export const routeApi = {
    /**
     * @param {Object} params - search, page, size
     */
    filterRoutes: (params) => { return axiosClient.get(BASE, { params }); },
    
    /**
     * @param {Object} data - routeName, totalKilometers, totalMinutes, active
    */
    createRoute: (data) => { return axiosClient.post(BASE, data); },

    /**
     * @param {Object} data - routeName, totalKilometers, totalMinutes, active, routeStops
    */
    createRouteWithStops: (data) => { return axiosClient.post(`${BASE}/with-stops`, data); },
    
    /**
     * @param {number|string} id 
     */
    getRouteDetail: (id) => { return axiosClient.get(`${BASE}/${id}`); },

    /**
     * @param {number|string} id 
     * @param {Object} data 
     */
    updateRouteInfo: (id, data) => { return axiosClient.put(`${BASE}/${id}`, data); },

    /**
     * @param {number|string} id
     */
    softDeleteRoute: (id) => { return axiosClient.patch(`${BASE}/${id}/soft-delete`); },

    /**
     * @param {number|string} id
     */
    restoreRoute: (id) => { return axiosClient.patch(`${BASE}/${id}/restore`); },

    /**
     * lấy danh sách routes (id, name) để hiển thị dropdown
     */
    getRoutesForDropdown: () => { return axiosClient.get(`${BASE}/dropdown`) }
};
