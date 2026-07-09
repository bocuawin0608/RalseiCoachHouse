import axiosClient from '../../../api/axiosClient';

export const authApi = {

  staffLogin: async (credentials) => {
    return await axiosClient.post('/auth/staff/login', {
      username: credentials.username,
      password: credentials.password
    });
  },

  /** Requests a staff-only temporary password email from the login page. */
  staffForgotPassword: async (payload) => {
    return await axiosClient.post('/auth/staff/forgot-password', {
      username: payload.username,
      email: payload.email
    });
  },

  logout: async (refreshToken) => {
    return await axiosClient.post('/auth/logout', { refreshToken });
  }
};
