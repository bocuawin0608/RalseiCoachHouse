import axiosClient from '../../../api/axiosClient';

const BASE_URL = '/v1/staff/trips/info';

export const staffTripInfoApi = {
    /**
     * Fetch upcoming trip rows for the ticket-staff trip-info screen.
     *
     * Arrays are serialized by axiosClient as repeated query params so checkbox
     * filters reach Spring as List<String>.
     */
    searchTrips: (params) => axiosClient.get(BASE_URL, { params }),
};
