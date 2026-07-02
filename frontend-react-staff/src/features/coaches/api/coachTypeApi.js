import axiosClient from '../../../api/axiosClient';
const BASE = '/v1/coach-types';

export const coachTypeApi = {
    filterCoachTypes: (params) => axiosClient.get(BASE, { params }),

    createCoachType: (data) => axiosClient.post(BASE, data),

    getCoachTypeDetail: (id) => axiosClient.get(`${BASE}/${id}`),

    updateCoachTypeInfo: (id, data) => axiosClient.patch(`${BASE}/${id}`, data),

    updateCoachTypePrice: (id, data) => axiosClient.put(`${BASE}/${id}/price`, data),

    updateCoachTypeSeatMap: (id, data) => axiosClient.patch(`${BASE}/${id}/seat-layout`, data),

    getPriceTimeline: (id) => axiosClient.get(`${BASE}/${id}/prices`),

    addPrice: (id, data) => axiosClient.post(`${BASE}/${id}/prices`, data),

    getDeactivationCheck: (id) => axiosClient.get(`${BASE}/${id}/deactivation-check`),

    getCoachTypesDropdown: () => axiosClient.get(`${BASE}/dropdown`),
};

export const INFINITE_END_YEAR = 9999;

export const isOpenEndedPrice = (dateStr) => {
    if (!dateStr) return false;
    return new Date(dateStr).getFullYear() >= INFINITE_END_YEAR;
};

export const formatPriceEndDate = (dateStr) => {
    if (!dateStr || isOpenEndedPrice(dateStr)) return 'Không giới hạn';
    return new Date(dateStr).toLocaleString('vi-VN');
};

export const PRICE_STATUS_LABELS = {
    UPCOMING: { text: 'Sắp áp dụng', bg: 'warning' },
    ACTIVE: { text: 'Đang áp dụng', bg: 'success' },
    EXPIRED: { text: 'Đã hết hạn', bg: 'secondary' },
};
