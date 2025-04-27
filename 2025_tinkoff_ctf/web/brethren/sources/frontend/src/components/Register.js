import React, { useState } from "react";
import { registerUser } from "../api";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {useNavigate} from "react-router-dom";

const Register = () => {
  const [formData, setFormData] = useState({ username: "", email: "", password: "" });
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const handleRegister = async (e) => {
    e.preventDefault();
    setError(null);

    try {
      await registerUser(formData);
      alert("Регистрация успешна! Теперь вы можете войти в систему.");
      navigate("/login");
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="register-form">
      <h2 className="subtitle has-text-centered">Регистрация</h2>
      {error && <p className="notification is-danger">{error}</p>}
      <form onSubmit={handleRegister}>
        <div className="field">
          <div className="control has-icons-left">
            <input 
              className="input" 
              type="text" 
              placeholder="Введите имя пользователя" 
              minlength="9"
              value={formData.username} 
              onChange={(e) => setFormData({ ...formData, username: e.target.value })} 
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
              type="email" 
              placeholder="Введите email" 
              minlength="9"
              value={formData.email} 
              onChange={(e) => setFormData({ ...formData, email: e.target.value })} 
              required 
            />
            <span className="icon is-small is-left">
              <FontAwesomeIcon icon="envelope" />
            </span>
          </div>
        </div>
        <div className="field">
          <div className="control has-icons-left">
            <input 
              className="input" 
              type="password" 
              placeholder="Введите пароль" 
              minlength="9"
              value={formData.password} 
              onChange={(e) => setFormData({ ...formData, password: e.target.value })} 
              required 
            />
            <span className="icon is-small is-left">
              <FontAwesomeIcon icon="lock" />
            </span>
          </div>
        </div>
        <div className="field">
          <div className="control has-text-centered">
            <button className="button is-success is-fullwidth">
              <span className="icon">
                <FontAwesomeIcon icon="user-plus" />
              </span>
              <span>Зарегистрироваться</span>
            </button>
          </div>
        </div>
      </form>
    </div>
  );
};

export default Register;
