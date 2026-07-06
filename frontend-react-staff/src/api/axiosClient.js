import axios from 'axios';
import { authStorage } from '../features/auth';

const axiosClient = axios.create({
    baseURL: '/api',
    headers: {'Content-Type': 'application/json'},
    withCredentials: true,
    timeout: 10000,
    paramsSerializer: (params) => {
        const searchParams = new URLSearchParams();
        Object.entries(params).forEach(([key, value]) => {
            if (Array.isArray(value)) {
                // Nếu là mảng (như statuses), lặp qua từng phần tử và append chung 1 key
                value.forEach(v => searchParams.append(key, v));
            } else if (value !== undefined && value !== null && value !== '') {
                // Bỏ qua các param rỗng như licensePlate=&coachTypeId= để URL sạch hơn
                searchParams.append(key, value);
            }
        });
        return searchParams.toString();
    }
});

axiosClient.interceptors.request.use(
    (config) => {
        const token = authStorage.getToken();
        if (token) {
            config.headers['Authorization'] = `Bearer ${token.replace(/['"]+/g, '')}`;    
        }
        return config;
    },
    (error) => Promise.reject(error)
);

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
    failedQueue.forEach(prom => {
        if (error) {
            prom.reject(error);
        } else {
            prom.resolve(token);
        }
    });
    failedQueue = [];
};

axiosClient.interceptors.response.use(
    (response) => response.data,
    async (error) => {
        const originalRequest = error.config;

        if (error.response) {
            const status = error.response.status;

            if (status === 401 && !originalRequest._retry) {
                if (originalRequest.url.includes('/auth/refresh-token')) {
                    handleAuthFailure();
                    return Promise.reject(error);
                }

                if (isRefreshing) {
                    return new Promise(function(resolve, reject) {
                        failedQueue.push({ resolve, reject });
                    }).then(token => {
                        originalRequest.headers['Authorization'] = `Bearer ${token.replace(/['"]+/g, '')}`; 
                        return axiosClient(originalRequest);
                    }).catch(err => Promise.reject(err));
                }

                originalRequest._retry = true;
                isRefreshing = true;

                const refreshToken = authStorage.getRefreshToken();
                
                if (!refreshToken) {
                    console.error("Không tìm thấy Refresh Token trong kho!");
                    handleAuthFailure();
                    return Promise.reject(error);
                }

                return new Promise(function (resolve, reject) {
                    axios.post(`${axiosClient.defaults.baseURL}/auth/refresh-token`, 
                        { refreshToken: refreshToken }, 
                        { headers: {'Content-Type': 'application/json'}, withCredentials: true }
                    )
                    .then(({ data }) => {
                        const newAccessToken = data.accessToken;
                        
                        authStorage.setToken(newAccessToken, true); 
                        
                        originalRequest.headers['Authorization'] = `Bearer ${newAccessToken.replace(/['"]+/g, '')}`; 
                        processQueue(null, newAccessToken);
                        resolve(axiosClient(originalRequest));
                    })
                    .catch((err) => {
                        console.error("Refresh Token xịt ròi:", err);
                        processQueue(err, null);
                        handleAuthFailure();
                        reject(err);
                    })
                    .finally(() => {
                        isRefreshing = false;
                    });
                });
            }

            switch (status) {
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

const handleAuthFailure = () => {
    authStorage.clearAll();
    window.location.href = "/staff/login";
}

export default axiosClient;