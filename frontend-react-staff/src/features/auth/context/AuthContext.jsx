import { createContext, useContext, useState } from 'react';
import { authApi } from '../api/authApi';
import { authStorage } from '../util/authStorage';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => authStorage.getUser());
  const [token, setToken] = useState(() => authStorage.getToken());
  const [loading, setLoading] = useState(false);

  const processAuthSuccess = (response, rememberMe = true) => {
    const { accessToken, refreshToken, username, roles } = response;
    const userData = { username, roles };
    
    authStorage.setToken(accessToken, rememberMe);
    authStorage.setRefreshToken(refreshToken, rememberMe);
    authStorage.setUser(userData, rememberMe);
    
    setToken(accessToken);
    setUser(userData);
    return response;
  };

  const loginStaff = async (credentials) => {
    setLoading(true);
    try {
      const response = await authApi.staffLogin(credentials);
      return processAuthSuccess(response, true); // Staff mặc định giữ phiên
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    authApi.logout(authStorage.getRefreshToken());
    authStorage.clearAll();
    setUser(null);
    setToken(null);
  };

  return (
    <AuthContext.Provider value={{ 
      user, token, loading, setLoading,          
      processAuthSuccess, loginStaff, logout 
    }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within an AuthProvider');
  return context;
};