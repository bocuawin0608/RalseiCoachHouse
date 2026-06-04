import axios from 'axios';

const axiosClient = axios.create({
    baseURL: 'https://localhost:9090/api',
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true,
    timeout: 10000,
});

axiosClient.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('accessToken');
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

axiosClient.interceptors.response.use(
    (response) => {
        return response.data;
    },
    (error) => {
        if (error.response) {
            const status = error.response.status;

            switch (status) {
                case 401:
                    console.error("Lỗi 401: Chưa đăng nhập hoặc phiên làm việc hết hạn!");
                    // Future để: localStorage.removeItem('accessToken'); window.location.href = '/login';
                    break;
                case 403:
                    console.error("Lỗi 403: Bạn không có quyền truy cập tính năng này!");
                    break;
                case 404:
                    console.error("Lỗi 404: Không tìm thấy tài nguyên trên server!");
                    break;
                case 500:
                    console.error("Lỗi 500: Hệ thống Backend đang gặp sự cố!");
                    break;
                default:
                    console.error(`Lỗi hệ thống xuất hiện: ${status}`);
            }
        } else {
            console.error("Lỗi mất kết nối mạng hoặc Server không hoạt động!");
        }
        return Promise.reject(error);
    }
);

export default axiosClient;