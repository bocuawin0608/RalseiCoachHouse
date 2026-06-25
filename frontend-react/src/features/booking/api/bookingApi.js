import axiosClient from "../../../api/axiosClient"
const BASE = '/v1/bookings';

export const bookingApi = {
    getSeatMap: (tripId) => { return axiosClient.get(`${BASE}/trips/${tripId}/seats`) },

    lockSeats: (tripId, data, holdToken) => { return axiosClient.post(`${BASE}/trips/${tripId}/seats/lock`, data, {
        headers: {'X-Booking-Session':holdToken}
    }) },

    releaseSeats: (tripId, data, holdToken) => { return axiosClient.post(`${BASE}/trips/${tripId}/seats/release`, data, {
        headers: {'X-Booking-Session':holdToken}
    }) }
}