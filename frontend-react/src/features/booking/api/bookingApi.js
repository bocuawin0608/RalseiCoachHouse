import axiosClient from "../../../api/axiosClient"
const BASE = '/v1/bookings';

export const bookingApi = {
    getSeatMap: (tripId) => { return axiosClient.get(`${BASE}/trips/${tripId}/seats`) },

    lockSeats: (tripId, data, holdToken) => { return axiosClient.post(`${BASE}/trips/${tripId}/seats/lock`, data, {
        headers: {'X-Booking-Session':holdToken}
    }) },

    releaseSeats: (tripId, data, holdToken) => { return axiosClient.post(`${BASE}/trips/${tripId}/seats/release`, data, {
        headers: {'X-Booking-Session':holdToken}
    }) },

    getStep2InitData: (tripId, holdToken) => {
        return axiosClient.get(`${BASE}/trips/${tripId}/step2-init-data`, {
            headers: { 'X-Booking-Session': holdToken }
        });
    },

    calculatePrice: (tripId, data, holdToken) => {
        return axiosClient.post(`${BASE}/trips/${tripId}/calculate-price`, data, {
            headers: { 'X-Booking-Session': holdToken }
        });
    },

    confirmBooking: (tripId, data, holdToken) => {
        return axiosClient.post(`${BASE}/trips/${tripId}/confirm`, data, {
            headers: { 'X-Booking-Session': holdToken }
        });
    },

    getPaymentPage: (transactionId, cancelToken) => {
        return axiosClient.get(`${BASE}/payments/${transactionId}`, {
            headers: cancelToken ? { 'X-Cancel-Token': cancelToken } : undefined,
        });
    },

    expirePayment: (transactionId, cancelToken) => {
        return axiosClient.post(`${BASE}/payments/${transactionId}/expire`, null, {
            headers: cancelToken ? { 'X-Cancel-Token': cancelToken } : undefined,
        });
    },

    cancelPayment: (transactionId, cancelToken) => {
        return axiosClient.post(`${BASE}/payments/${transactionId}/cancel`, null, {
            headers: cancelToken ? { 'X-Cancel-Token': cancelToken } : undefined,
        });
    },
}
