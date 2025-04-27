import React from "react";
import { Link } from "react-router-dom";
import Register from "../components/Register";
import { IMAGES } from "../utils/imageUtils";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const RegisterPage = () => {
  return (
    <div className="columns is-centered is-vcentered" style={{ minHeight: '60vh', margin: 0 }}>
      <div className="column is-5">
        <div className="box">
          <div className="has-text-centered mb-3">
            <img 
              src={IMAGES.CAPYBARA1} 
              alt="Capybara Logo" 
              className="capybara-logo is-rounded" 
              style={{ width: '120px' }}
            />
          </div>
          <h1 className="title is-3 has-text-centered mb-4">Регистрация в ИКиЗН</h1>
          <div className="columns is-centered">
            <div className="column is-8">
              <Register />
            </div>
          </div>
          <div className="has-text-centered mt-4">
            <p className="mb-2">Уже есть аккаунт? <Link to="/login" className="has-text-weight-bold">Войти</Link></p>
            <div className="has-text-centered">
              <Link to="/" className="has-text-weight-bold">
                <span className="icon-text">
                  <span className="icon">
                    <FontAwesomeIcon icon="home" />
                  </span>
                  <span>На главную</span>
                </span>
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage; 