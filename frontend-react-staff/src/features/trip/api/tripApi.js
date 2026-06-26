import axiosClient from '../../../api/axiosClient';

export const tripApi = {
    /** Fetch paginated trip summaries with optional date filter (Manager) */
    filterTrips: async (params) => {
        const response = await axiosClient.get('/v1/manager/trips/summaries', { params });
        return response;
    },

    /** Create a new trip (Manager) */
    createTrip: async (data) => {
        const response = await axiosClient.post('/v1/manager/trips/create', data);
        return response;
    },

    /** Update an existing trip by ID (Manager) */
    updateTrip: async (id, data) => {
        const response = await axiosClient.put(`/v1/manager/trips/update/${id}`, data);
        return response;
    },

    /** Soft-delete (cancel) a trip by ID (Manager) */
    deleteTrip: async (id) => {
        const response = await axiosClient.delete(`/v1/manager/trips/delete/${id}`);
        return response;
    }
};