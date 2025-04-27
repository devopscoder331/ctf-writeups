import React, { useState } from "react";
import { Link } from "react-router-dom";
import { IMAGES } from "../utils/imageUtils";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useAuth } from "../utils/AuthContext";

const Navbar = () => {
  const [isActive, setIsActive] = useState(false);
  const { isAuthenticated, logout, user } = useAuth();

  return (
    <nav className="navbar is-transparent mb-5" role="navigation" aria-label="main navigation">
      <div className="container">
        <div className="navbar-brand">
          <Link className="navbar-item" to="/">
            <img 
              src={IMAGES.CAPYBARA1} 
              alt="Capybara Logo" 
              className="capybara-logo mr-2" 
              style={{ maxHeight: '50px' }}
            />
            <span className="title is-4 has-text-weight-bold">ИКиЗН</span>
          </Link>

          <a
            role="button"
            className={`navbar-burger ${isActive ? 'is-active' : ''}`}
            aria-label="menu"
            aria-expanded="false"
            onClick={() => setIsActive(!isActive)}
          >
            <span aria-hidden="true"></span>
            <span aria-hidden="true"></span>
            <span aria-hidden="true"></span>
          </a>
          <div className={`navbar-menu ${isActive ? 'is-active' : ''}`}>
          <div className="navbar-end">
            <Link to="/" className="navbar-item">
              <span className="icon-text">
                <span className="icon">
                  <FontAwesomeIcon icon="home" />
                </span>
                <span>Главная</span>
              </span>
            </Link>
            
            {isAuthenticated && (
              <>
                {user?.is_admin && (
                  <Link to="/admin" className="navbar-item">
                    <span className="icon-text">
                      <span className="icon">
                        <FontAwesomeIcon icon="shield-alt" />
                      </span>
                      <span>Админ панель</span>
                    </span>
                  </Link>
                )}
                <a className="navbar-item" onClick={logout}>
                  <span className="icon-text">
                    <span className="icon">
                      <FontAwesomeIcon icon="sign-out-alt" />
                    </span>
                    <span>Выйти</span>
                  </span>
                </a>
              </>
            )}
            
            <div className="navbar-item">
              <div className="buttons">
                {isAuthenticated ? (
                  <Link to="/dashboard" className="button is-primary">
                    <span className="icon">
                      <FontAwesomeIcon icon="ticket-alt" />
                    </span>
                    <span>Мои билеты</span>
                  </Link>
                ) : (
                  <Link to="/login" className="button is-primary">
                    <span className="icon">
                      <FontAwesomeIcon icon="sign-in-alt" />
                    </span>
                    <span>Войти</span>
                  </Link>
                )}
              </div>
            </div>
          </div>
        </div>
        </div>


      </div>
    </nav>
  );
};

export default Navbar; 
