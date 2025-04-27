import React, { useState } from 'react';
import { useAuth } from '../utils/AuthContext';
import { Navigate, useNavigate } from 'react-router-dom';
import { getFlag } from '../api';

const AdminPage = () => {
  const { user } = useAuth();
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  if (!user?.is_admin) {
    return <Navigate to="/dashboard" replace />;
  }

  const handleGetFlag = async () => {
    try {
      setError(null);
      const result = await getFlag();
      localStorage.setItem('capturedFlag', result.flag);
      navigate('/');
    } catch (err) {
      setError(err.message);
      console.error('Error getting flag:', err);
    }
  };

  return (
    <div className="section">
      <div className="container">
        <h1 className="title">Панель администратора</h1>
        <div className="box">
          <h2 className="subtitle">Управление системой</h2>
          <div className="content">
            <p>Добро пожаловать в панель администратора!</p>
            <div className="notification is-warning mb-4">
              <p>⚠️ Внимание: После получения флага у вас будет только 30 секунд, чтобы его скопировать!</p>
            </div>
            <div className="buttons">
              <button 
                className="button is-primary" 
                onClick={handleGetFlag}
              >
                Подменить билеты
              </button>
            </div>
            {error && (
              <div className="notification is-danger mt-3">
                <p>Ошибка: {error}</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminPage; 