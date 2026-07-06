import {
  signInWithPopup,
  GoogleAuthProvider,
  FacebookAuthProvider,
} from 'firebase/auth';
import { auth } from '../config/firebase';
import axiosClient from '../../../api/axiosClient';

const sendToBackend = async (idToken, username, endpoint, extraData = {}) => {
  const payload = { idToken, username, ...extraData };
  return await axiosClient.post(`/auth${endpoint}`, payload, {
    skipAuth: true,
  });
};

export const authApi = {
  // CUSTOMER: Login Phone (Sau khi đã verify OTP và có userCredential)
  customerPhoneLogin: async (userCredential, originalPhone) => {
    const idToken = await userCredential.user.getIdToken();
    return await sendToBackend(idToken, originalPhone, '/customer/login');
  },

  // CUSTOMER: Register Phone (Sau khi đã verify OTP ở UI)
  customerPhoneRegister: async (userCredential, customerData) => {
    const idToken = await userCredential.user.getIdToken();
    return await sendToBackend(idToken, customerData.username, '/customer/register', {
      customerName: customerData.customerName,
      email: customerData.email || null,
    });
  },

  // CUSTOMER: Social Login với Google (Tự động register ở BE nếu chưa có)
  signInWithGoogle: async () => {
    const provider = new GoogleAuthProvider();
    provider.setCustomParameters({ prompt: 'select_account' });
    try {
      const result = await signInWithPopup(auth, provider);
      return await sendToBackend(await result.user.getIdToken(), result.user.email, '/customer/login');
    } catch (error) {
      throw handleSocialError(error, 'Google');
    }
  },

  // CUSTOMER: Social Login với Facebook (Tự động register ở BE nếu chưa có)
  signInWithFacebook: async () => {
    const provider = new FacebookAuthProvider();
    provider.addScope('email');
    try {
      const result = await signInWithPopup(auth, provider);
      const username = result.user.email || `fb_${result.user.uid.substring(0, 8)}`;
      return await sendToBackend(await result.user.getIdToken(), username, '/customer/login');
    } catch (error) {
      throw handleSocialError(error, 'Facebook');
    }
  },

  // CHUNG để logout
  logout: async (refreshToken) => {
    return await axiosClient.post('/auth/logout', { refreshToken });
  }
};

const handleSocialError = (error, providerName) => {
  if (error.code === 'auth/popup-closed-by-user') return null; 
  throw new Error(`Đăng nhập ${providerName} thất bại!`);
};
