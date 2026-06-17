import axiosClient from '../../../api/axiosClient';
const BASE = '/v1/coach-types';

export const coachTypeApi = {
    /**
     * @param {Object} params - Chứa các trường filter (coachTypeName, min/maxPrice, min/maxSeat, isActive, page, size)
     */
    filterCoachTypes: (params) => { return axiosClient.get(BASE, { params }); },
    
    /**
     * @param {Object} data - Chứa thông tin tạo mới (coachTypeName, seatLayout, seatPrice)
    */
   createCoachType: (data) => { return axiosClient.post(BASE, data); },
    
    /**
     * @param {number|string} id - ID của CoachType cần xem chi tiết
     */
    getCoachTypeDetail: (id) => { return axiosClient.get(`${BASE}/${id}`); },

    /**
     * @param {number|string} id - ID của CoachType cần sửa
     * @param {Object} data - Chứa thông tin cần cập nhật (coachTypeName, isActive)
     */
    updateCoachTypeInfo: (id, data) => { return axiosClient.patch(`${BASE}/${id}`, data); },

    /**
     * @param {number|string} id - ID của CoachType cần sửa
     * @param {Object} data - Chứa thông tin cần cập nhật (seatPrice)
     */
    updateCoachTypePrice: (id, data) => { return axiosClient.put(`${BASE}/${id}/price`, data); },

    /**
     * @param {number|string} id - ID của CoachType cần sửa
     * @param {Object} data - Chứa thông tin cần cập nhật (seatLayout)
     */
    updateCoachTypeSeatMap: (id, data) => { return axiosClient.patch(`${BASE}/${id}/seat-layout`, data); },

    /**
     * nhận về danh sách coach type (id, name) để xổ ra dropdown
     */
    getCoachTypesDropdown: () => { return axiosClient.get(`${BASE}/dropdown`); }
};