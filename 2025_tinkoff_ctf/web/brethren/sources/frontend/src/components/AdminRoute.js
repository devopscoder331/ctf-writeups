import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../utils/AuthContext';

/**
 * AdminRoute component that checks if user is authenticated and is an admin
 * Redirects to dashboard if not admin, or to login if not authenticated
 */
const AdminRoute = ({ children }) => {
  const { isAuthenticated, user, loading } = useAuth();
  
  if (loading) {
    return <div className="has-text-centered p-5">Загрузка...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!user?.is_admin) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
};

export default AdminRoute; 