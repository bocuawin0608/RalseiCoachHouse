import axiosClient from '../../../api/axiosClient';

export const cargoTrackingApi = {
    trackByCode: (ticketCode) => axiosClient.get(`/v1/cargo-tracking/${ticketCode}`),
};
