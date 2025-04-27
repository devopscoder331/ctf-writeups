import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../utils/AuthContext';

/**
 * ProtectedRoute component that checks if user is authenticated
 * and redirects to login page if not
 */
const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();
  
  if (loading) {
    return <div className="has-text-centered p-5">Загрузка...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return children;
};

export default ProtectedRoute; 