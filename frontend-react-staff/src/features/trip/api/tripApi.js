import axiosClient from '../../../api/axiosClient';

export const tripApi = {
    /** Fetch paginated trip summaries with optional date filter (Manager) */
    filterTrips: async (params) => {
        const response = await axiosClient.get('/v1/manager/trips/summaries', { params });
        return response;
    },

    getIncidents: (date) => axiosClient.get('/v1/manager/trips/incidents', { params: { date } }),

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

    /** Load coaches that are genuinely free for the complete trip window. */
    getAvailableCoaches: (params) => axiosClient.get('/v1/manager/trips/available-coaches', { params }),

    /** Load drivers that are genuinely free for the complete trip window. */
    getAvailableDrivers: (params) => axiosClient.get('/v1/manager/trips/available-drivers', { params }),

    /** Load attendants that are genuinely free for the complete trip window. */
    getAvailableAttendants: (params) => axiosClient.get('/v1/manager/trips/available-attendants', { params }),

    /** Rescue resources are evaluated against server time, never the old departure time. */
    getIncidentReplacementCoaches: (tripId, routeId) => axiosClient.get(
        `/v1/manager/trips/${tripId}/replacement-coaches`, { params: { routeId } }
    ),

    getIncidentReplacementDrivers: (tripId) => axiosClient.get(
        `/v1/manager/trips/${tripId}/replacement-drivers`
    ),

    replaceIncidentCoach: (tripId, data) => axiosClient.put(
        `/v1/manager/trips/${tripId}/replace-incident-coach`, data
    ),

    /** Soft-delete (cancel) a trip by ID (Manager) */
    deleteTrip: async (id) => {
        const response = await axiosClient.delete(`/v1/manager/trips/delete/${id}`);
        return response;
    }
};
