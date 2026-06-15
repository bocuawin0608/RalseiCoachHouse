import axiosClient from '../../../api/axiosClient';

export const cargoTypePriceApi = {
    getAllCargoTypePrices: async (search, page, size = 10) => {
        const response = await axiosClient.get('/v1/manager/cargo-type-prices', {
            params: {
                search,
                page,
                size
            }
        });
        return response;
    },

    createCargoTypePrice: async (data) => {
        const response = await axiosClient.post('/v1/manager/cargo-type-prices', data);
        return response;
    },

    updateCargoTypePrice: async (id, data) => {
        const response = await axiosClient.put(`/v1/manager/cargo-type-prices/${id}`, data);
        return response;
    },

    deleteCargoTypePrice: async (id) => {
        const response = await axiosClient.delete(`/v1/manager/cargo-type-prices/${id}`);
        return response;
    }
};
