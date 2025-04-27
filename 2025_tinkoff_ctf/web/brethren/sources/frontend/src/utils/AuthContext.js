import React, { createContext, useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchUserInfo } from '../api';

const AuthContext = createContext();

export const useAuth = () => {
  return useContext(AuthContext);
};

export const AuthProvider = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  const updateUserInfo = async () => {
    const token = localStorage.getItem('token');
    if (token) {
      try {
        const userData = await fetchUserInfo();
        setUser(userData);
        setIsAuthenticated(true);
      } catch (error) {
        console.error('Error fetching user info:', error);
        setIsAuthenticated(false);
        setUser(null);
      }
    } else {
      setIsAuthenticated(false);
      setUser(null);
    }
    setLoading(false);
  };

  useEffect(() => {
    updateUserInfo();
  }, []);

  useEffect(() => {
    const handleStorageChange = (e) => {
      if (e.key === 'token') {
        updateUserInfo();
      }
    };

    window.addEventListener('storage', handleStorageChange);
    return () => window.removeEventListener('storage', handleStorageChange);
  }, []);

  const login = (token, userData) => {
    localStorage.setItem('token', token);
    setIsAuthenticated(true);
    setUser(userData);
  };

  const logout = () => {
    localStorage.removeItem('token');
    setIsAuthenticated(false);
    setUser(null);
    navigate('/');
  };

  const value = {
    isAuthenticated,
    user,
    loading,
    login,
    logout,
    updateUserInfo
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
};

export default AuthContext; 