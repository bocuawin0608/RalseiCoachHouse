import axiosClient from '../../../api/axiosClient';

const BASE_URL = '/v1/ticket-staff/cargo-tickets';

export const cargoTicketApi = {
    getCargoTickets: (params) => axiosClient.get(BASE_URL, { params }),
    getFormOptions: (params) => axiosClient.get(`${BASE_URL}/form-options`, { params }),
    getCargoTicket: (id) => axiosClient.get(`${BASE_URL}/${id}`),
    getCargoTicketDetails: (id) => axiosClient.get(`${BASE_URL}/${id}/details`),
    createCargoTicket: (data) => axiosClient.post(`${BASE_URL}/with-details`, data),
    updateCargoTicket: (id, data) => axiosClient.put(`${BASE_URL}/${id}`, data),
    createCargoTicketDetail: (ticketId, data) => axiosClient.post(`${BASE_URL}/${ticketId}/details`, data),
    updateCargoTicketDetail: (detailId, data) => axiosClient.put(`${BASE_URL}/details/${detailId}`, data),
    deleteCargoTicketDetail: (detailId) => axiosClient.delete(`${BASE_URL}/details/${detailId}`),
    disableCargoTicket: (id) => axiosClient.put(`${BASE_URL}/${id}/disable`),
    searchContacts: (phone) => axiosClient.get(`${BASE_URL}/contacts/search`, { params: { phone } }),
    completePayment: (id) => axiosClient.put(`${BASE_URL}/${id}/complete-payment`),
    getTripsByStops: (params) => axiosClient.get(`${BASE_URL}/trips-by-stops`, { params })
};
