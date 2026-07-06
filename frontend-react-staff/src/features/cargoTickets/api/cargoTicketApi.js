import axiosClient from '../../../api/axiosClient';

const BASE_URL = '/v1/ticket-staff/cargo-tickets';

export const cargoTicketApi = {
    getCargoTickets: (params) => axiosClient.get(BASE_URL, { params }),
    getFormOptions: (params) => axiosClient.get(`${BASE_URL}/form-options`, { params }),
    getCargoTicket: (id) => axiosClient.get(`${BASE_URL}/${id}`),
    createCargoTicket: (data) => axiosClient.post(BASE_URL, data),
    updateCargoTicket: (id, data) => axiosClient.put(`${BASE_URL}/${id}`, data),
    disableCargoTicket: (id) => axiosClient.put(`${BASE_URL}/${id}/disable`)
};
