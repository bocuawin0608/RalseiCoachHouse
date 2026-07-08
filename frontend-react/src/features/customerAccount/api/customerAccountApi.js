import axiosClient from '../../../api/axiosClient';

/**
 * Customer account self-service API boundary.
 * Pages call this module instead of hard-coding endpoint strings.
 */
export const customerAccountApi = {
  /** Loads the signed-in customer's profile. */
  getProfile: () => axiosClient.get('/v1/customer/me'),

  /** Sends the customer profile update command to the backend PATCH endpoint. */
  updateProfile: (payload) => axiosClient.patch('/v1/customer/me', payload),

  /** Soft-deactivates the signed-in customer account. */
  deactivateAccount: () => axiosClient.delete('/v1/customer/me'),
};
