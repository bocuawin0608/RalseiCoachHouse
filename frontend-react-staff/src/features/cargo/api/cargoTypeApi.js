import axiosClient from "../../../api/axiosClient";

/**
 * API wrapper for the combined cargo type management screen.
 * Unit and price are submitted with the cargo type because they are edited as
 * one staff-facing surcharge record.
 */
export const cargoTypeApi = {
    filterCargoTypes: async (params) => {
        const response = await axiosClient.get('/v1/manager/cargo-types', { params });
        return response;
    },
    createCargoType: async (data) => {
        const response = await axiosClient.post('/v1/manager/cargo-types', data);
        return response;
    },
    updateCargoTypeInfo: async (id, data) => {
        const response = await axiosClient.put(`/v1/manager/cargo-types/${id}`, data);
        return response;
    },
    toggleCargoTypeStatus: async (id, isActive) => {
        if (isActive) {
            return await axiosClient.patch(`/v1/manager/cargo-types/${id}/restore`);
        } else {
            return await axiosClient.patch(`/v1/manager/cargo-types/${id}/soft-delete`);
        }
    }
};
