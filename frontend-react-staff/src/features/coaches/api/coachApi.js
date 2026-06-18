import axiosClient from '../../../api/axiosClient';
const BASE = '/v1/coaches';

export const coachApi = {
    /**
     * @param {Object} params - Chứa các trường filter (licensePlate, statuses (dsach), coachTypeId (hien thi coachTypeName), routeName, page, size)
     */
    filterCoaches: (params) => { return axiosClient.get(BASE, { params }); },

    /**
     * @param {Object} data - Chứa thông tin tạo mới (coachTypeId, routeId, licensePlate, manufacturer, year)
     */
    createCoach: (data) => { return axiosClient.post(BASE, data); },

    /**
     * @param {number|string} id - ID của Coach cần sửa
     * @param {Object} data - Chứa thông tin cần cập nhật (routeId, coachTypeId, licensePlate, manufacturer, year, status)
     */
    updateCoachInfo: (id, data) => { return axiosClient.put(`${BASE}/${id}`, data); },

    getCoachDetailForView: (id) => { return axiosClient.get(`${BASE}/${id}/view-detail`)},

    getCoachDetailForEdit: (id) => { return axiosClient.get(`${BASE}/${id}/view-edit`) },
}