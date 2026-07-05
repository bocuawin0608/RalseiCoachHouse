import axiosClient from '../../../api/axiosClient';

export const cargoTrackingApi = {
    trackByCode: (ticketCode) => axiosClient.get(`/v1/cargo-tracking/${ticketCode}`),
    getMyCargo: (params) => axiosClient.get('/v1/cargo-tracking/my-cargo', { params }),
    getMyCargoDetail: (id) => axiosClient.get(`/v1/cargo-tracking/my-cargo/${id}`),
};
