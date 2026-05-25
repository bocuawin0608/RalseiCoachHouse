import axiosClient from '../api/axiosClient';

/** MODULE QUẢN LÝ SƠ ĐỒ GHẾ
    1. GET    /api/seat-layouts?search=...&page=...  -> [Layout] Tìm kiếm + Phân trang sơ đồ
    2. GET    /api/seat-layouts/{id}                 -> [Layout + Seat] Lấy chi tiết sơ đồ & Ma trận ghế bên trong
    3. POST   /api/seat-layouts                      -> [Layout + Seat + Price] Khởi tạo trọn gói cả cụm 3 bảng
    4. PUT    /api/seat-layouts/{id}                 -> [Layout] Cập nhật thông tin khung sơ đồ
    5. PATCH  /api/seat-layouts/{id}/status          -> [Layout] Bật/Tắt cả cái sơ đồ xe (aka xóa mềm)

    6. PATCH  /api/seat-layouts/{id}/seats/{seatId}         -> [Seat] Chỉnh sửa mã ghế hoặc Tọa độ hàng/cột
    7. PATCH  /api/seat-layouts/{id}/seats/{seatId}/status  -> [Seat] Bật/Tắt 1 ghế cụ thể (aka xóa mềm)
    8. POST   /api/seat-layouts/{id}/rows                   -> [Seat] Mở rộng quy mô: Thêm hẳn 1 hàng ghế
    9. POST   /api/seat-layouts/{id}/cols                   -> [Seat] Mở rộng quy mô: Thêm hẳn 1 cột ghế

    10. GET   /api/seat-layouts/{id}/prices          -> [Price] Xem lịch sử các mốc giá của sơ đồ này
    11. POST   /api/seat-layouts/{id}/prices         -> [Price] Áp mốc giá mới (ngầm cơ chế chỉnh start/endEffectiveDate)
 */

export const seatLayoutService = {
    /**
     * @param {Object} params - Chứa các trường filter (seatLayoutName, min/maxPrice, min/maxSeat, page, size)
     */
    filterSeatLayouts: (params) => {
        const url = '/seat-layouts';
        return axiosClient.get(url, { params });
    },

    /**
     * @param {number|string} id - ID của SeatLayout cần xem chi tiết
     */
    getSeatLayoutDetail: (id) => {
        const url = `/seat-layouts/${id}`;
        return axiosClient.get(url);
    },

    /**
     * @param {Object} data - Payload chứa thông tin tạo mới (tên sơ đồ, số hàng ghế, số cột ghế, cấu hình giá)
     */
    createSeatLayout: (data) => {
        const url = '/seat-layouts';
        return axiosClient.post(url, data);
    },

    /**
     * @param {number|string} id - ID của SeatLayout cần sửa
     * @param {Object} data - Dữ liệu mới cần cập nhật
     */
    updateSeatLayout: (id, data) => {
        const url = `/seat-layouts/${id}`;
        return axiosClient.put(url, data);
    },

    /**
     * @param {number|string} id - ID của SeatLayout cần bật/tắt status
     */
    toggleSeatLayoutStatus: (id) => {
        const url = `/seat-layouts/${id}/status`;
        return axiosClient.patch(url); 
    }
};