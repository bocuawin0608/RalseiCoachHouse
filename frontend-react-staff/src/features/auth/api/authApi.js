import axiosClient from '../../../api/axiosClient';

export const authApi = {

  staffLogin: async (credentials) => {
    return await axiosClient.post('/auth/staff/login', {
      username: credentials.username,
      password: credentials.password
    });
  },

  logout: async (refreshToken) => {
    return await axiosClient.post('/auth/logout', { refreshToken });
  }
};