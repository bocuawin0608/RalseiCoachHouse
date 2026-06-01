import axiosClient from '../api/axiosClient';

export const seatLayoutService = {
    /**
     * @param {Object} params - Chứa các trường filter (seatLayoutName, min/maxPrice, min/maxSeat, isActive, page, size)
     */
    filterSeatLayouts: (params) => {
        const url = '/seat-layouts';
        return axiosClient.get(url, { params });
    },
    
    /**
     * @param {Object} data - Chứa thông tin tạo mới (seatLayoutName, totalRows, totalCols, seatPrice)
    */
   createSeatLayout: (data) => {
       const url = '/seat-layouts';
       return axiosClient.post(url, data);
    },
    
    /**
     * @param {number|string} id - ID của SeatLayout cần xem chi tiết
     */
    getSeatLayoutDetail: (id) => {
        const url = `/seat-layouts/${id}`;
        return axiosClient.get(url);
    },

    /**
     * @param {number|string} id - ID của SeatLayout cần sửa
     * @param {Object} data - Chứa thông tin cần cập nhật (seatLayoutName, isActive)
     */
    updateSeatLayoutInfo: (id, data) => {
        const url = `/seat-layouts/${id}`;
        return axiosClient.patch(url, data);
    },
};