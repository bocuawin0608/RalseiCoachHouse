import axiosClient from '../../../api/axiosClient';

export const coachStopApi = {
    getAllCoachStops: async (search, isActive, page, size = 10) => {
        const response = await axiosClient.get('/v1/coach-stops', {
            params: {
                search,
                isActive,
                page,
                size
            }
        });
        return response;
    },

    getCoachStopById: async (id) => {
        const response = await axiosClient.get(`/v1/coach-stops/${id}`);
        return response;
    },

    createCoachStop: async (data) => {
        const response = await axiosClient.post('/v1/coach-stops', data);
        return response;
    },

    updateCoachStop: async (id, data) => {
        const response = await axiosClient.put(`/v1/coach-stops/${id}`, data);
        return response;
    },

    disableCoachStop: async (id) => {
        const response = await axiosClient.patch(`/v1/coach-stops/${id}/soft-delete`);
        return response;
    },

    restoreCoachStop: async (id) => {
        const response = await axiosClient.patch(`/v1/coach-stops/${id}/restore`);
        return response;
    }
};
