import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { loginUser, fetchUserInfo } from "../api";
import { useAuth } from "../utils/AuthContext";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const Login = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const { login } = useAuth();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError(null);

    try {
      const token = await loginUser(username, password);
      const userInfo = await fetchUserInfo();
      login(token, userInfo);
      navigate(userInfo.is_admin ? "/admin" : "/dashboard");
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="login-form">
      <h2 className="subtitle has-text-centered">Вход</h2>
      {error && <p className="notification is-danger">{error}</p>}
      <form onSubmit={handleLogin}>
        <div className="field">
          <div className="control has-icons-left">
            <input 
              className="input" 
              type="text" 
              placeholder="Введите имя пользователя" 
              value={username} 
              onChange={(e) => setUsername(e.target.value)} 
              required 
            />
            <span className="icon is-small is-left">
              <FontAwesomeIcon icon="user" />
            </span>
          </div>
        </div>
        <div className="field">
          <div className="control has-icons-left">
            <input 
              className="input" 
              type="password" 
              placeholder="Введите пароль" 
              value={password} 
              onChange={(e) => setPassword(e.target.value)} 
              required 
            />
            <span className="icon is-small is-left">
              <FontAwesomeIcon icon="lock" />
            </span>
          </div>
        </div>
        <div className="field">
          <div className="control has-text-centered">
            <button className="button is-primary is-fullwidth">
              <span className="icon">
                <FontAwesomeIcon icon="sign-in-alt" />
              </span>
              <span>Войти</span>
            </button>
          </div>
        </div>
      </form>
    </div>
  );
};

export default Login;
