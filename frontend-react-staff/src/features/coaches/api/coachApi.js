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
}