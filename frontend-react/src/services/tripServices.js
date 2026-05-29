import axiosClient from '../api/axiosClient'; // ✅ Đúng rồi, giữ nguyên

export const tripService = {
    searchTrips: async (searchParams) => {
        try {
            const responseData = await axiosClient.get('/v1/trips/home', {
                params: {
                    start: searchParams.start || '',
                    end: searchParams.end || '',
                    route: searchParams.route || `${searchParams.start} - ${searchParams.end}`,
                    date: searchParams.date || '',   // ✅ Thêm date
                    page: searchParams.page ?? 0,
                    size: searchParams.size || 10
                }
            });

            console.log("Dữ liệu thông mạch đổ về:", responseData);
            return responseData;

        } catch (error) {
            console.error("Lỗi nghẽn mạch tại tầng Trip Service:", error);
            throw error;
        }
    }
};