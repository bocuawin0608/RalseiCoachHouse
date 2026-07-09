import axiosClient from '../../../api/axiosClient';

/**
 * Staff account self-service API boundary.
 * Staff profile pages call this module instead of hard-coding endpoint strings.
 */
export const staffAccountApi = {
  /** Loads the signed-in staff member's profile. */
  getProfile: () => axiosClient.get('/v1/staff/me'),

  /** Sends editable staff profile fields to the backend PATCH endpoint. */
  updateProfile: (payload) => axiosClient.patch('/v1/staff/me', payload),

  /** Changes the signed-in staff member's local password. */
  changePassword: (payload) => axiosClient.post('/v1/staff/me/password', payload),
};
