import axiosClient from '../../../api/axiosClient';

export const refundApi = {
    searchPassenger(params) {
        return axiosClient.get('/v1/refunds/passenger', { params });
    },

    getPassengerDetail(refundId) {
        return axiosClient.get(`/v1/refunds/passenger/${refundId}`);
    },

    completePassenger(refundId, payload) {
        return axiosClient.patch(`/v1/refunds/passenger/${refundId}/complete`, payload);
    },
};
