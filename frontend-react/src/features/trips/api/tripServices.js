import axiosClient from '../../../api/axiosClient';

export const tripService = {
    searchTrips: async (searchParams) => {
        try {
            // Khởi tạo các tham số cơ bản bắt buộc
            const params = {
                date: searchParams.date,
                route: searchParams.route,
                page: searchParams.page ?? 0,
                size: searchParams.size ?? 10
            };

            // Nếu cờ nâng cao được bật hoặc có bất kỳ bộ lọc nào được chọn
            if (searchParams.isAdvanced) {
                params.advanced = 'true';
                if (searchParams.timeSlots) params.timeSlots = searchParams.timeSlots;
                if (searchParams.layouts) params.layouts = searchParams.layouts;
                if (searchParams.minPrice !== undefined && searchParams.minPrice !== null) params.minPrice = searchParams.minPrice;
                if (searchParams.maxPrice !== undefined && searchParams.maxPrice !== null) params.maxPrice = searchParams.maxPrice;
            }

            const responseData = await axiosClient.get('/v1/trips/home', { params });
            console.log("Dữ liệu thông mạch đổ về:", responseData);
            return responseData;
        } catch (error) {
            console.error("Lỗi nghẽn mạch tại tầng Trip Service:", error);
            throw error;
        }
    }
};