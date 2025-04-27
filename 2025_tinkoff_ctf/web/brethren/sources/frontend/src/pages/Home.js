import React, { useEffect, useState } from 'react';
import { Link } from "react-router-dom";
import { IMAGES } from "../utils/imageUtils";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useAuth } from "../utils/AuthContext";

const Home = () => {
  const { isAuthenticated } = useAuth();
  const [flag, setFlag] = useState(null);

  useEffect(() => {
    const savedFlag = localStorage.getItem('capturedFlag');
    if (savedFlag) {
      setFlag(savedFlag);
      const timer = setTimeout(() => {
        setFlag(null);
        localStorage.removeItem('capturedFlag');
      }, 30000);

      return () => clearTimeout(timer);
    }
  }, []);

  return (
    <div className="container">
      <div className="columns is-centered">
        <div className="column is-10">
          <div className="box mt-6 p-5">
            <div className="columns is-vcentered">
              <div className="column is-5">
                <figure className="image">
                  <img 
                    src={IMAGES.CAPYBARA1} 
                    alt="Capybara" 
                    className="is-rounded" 
                    style={{ maxWidth: '300px', margin: '0 auto' }}
                  />
                </figure>
              </div>
              <div className="column is-7">
                <h1 className="title is-2 has-text-centered">Институт капибар и забавных наук</h1>
                <h2 className="subtitle is-4 has-text-centered mb-4">Мир знаний и капибар</h2>
                <p className="has-text-centered is-size-5 mb-3">
                  Добро пожаловать в Институт капибар и забавных наук!
                </p>
                <p className="has-text-centered mb-3">
                  Здесь вы можете получить билеты на экзамены в нашем престижном институте.
                </p>
                <p className="has-text-centered mb-5">
                  Для того, чтобы получить билет, вам нужно будет зарегистрироваться и войти в систему.
                </p>
                <div className="buttons is-centered mt-5">
                  {!isAuthenticated ? (
                    <>
                      <Link to="/login" className="button is-primary is-medium is-rounded has-text-centered">
                        <span className="icon">
                          <FontAwesomeIcon icon="sign-in-alt" />
                        </span>
                        <span>Войти</span>
                      </Link>
                      <Link to="/register" className="button is-success is-medium is-rounded has-text-centered">
                        <span className="icon">
                          <FontAwesomeIcon icon="user-plus" />
                        </span>
                        <span>Зарегистрироваться</span>
                      </Link>
                    </>
                  ) : (
                    <Link to="/dashboard" className="button is-primary is-medium is-rounded has-text-centered">
                      <span className="icon">
                        <FontAwesomeIcon icon="ticket-alt" />
                      </span>
                      <span>Мои билеты</span>
                    </Link>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="columns is-multiline mt-6">
        <div className="column is-4">
          <div className="card equal-height">
            <div className="card-image">
              <figure className="image is-4by3">
                <img src={IMAGES.CAPYBARA2} alt="Capybara in water" />
              </figure>
            </div>
            <div className="card-content">
              <p className="title is-4">
                <span className="icon-text">
                  <span className="icon has-text-primary">
                    <FontAwesomeIcon icon="book" />
                  </span>
                  <span>Экзамены</span>
                </span>
              </p>
              <p className="subtitle is-6">Подготовка к экзаменам</p>
              <div className="content">
                Получите доступ к экзаменационным билетам и подготовьтесь к успешной сдаче экзаменов в нашем институте.
              </div>
            </div>
          </div>
        </div>
        
        <div className="column is-4">
          <div className="card equal-height">
            <div className="card-image">
              <figure className="image is-4by3">
                <img src={IMAGES.CAPYBARA3} alt="Capybara group" />
              </figure>
            </div>
            <div className="card-content">
              <p className="title is-4">
                <span className="icon-text">
                  <span className="icon has-text-success">
                    <FontAwesomeIcon icon="users" />
                  </span>
                  <span>Сообщество</span>
                </span>
              </p>
              <p className="subtitle is-6">Присоединяйтесь к нам</p>
              <div className="content">
                Станьте частью нашего дружного сообщества капибар и студентов, изучающих забавные науки.
              </div>
            </div>
          </div>
        </div>
        
        <div className="column is-4">
          <div className="card equal-height">
            <div className="card-image">
              <figure className="image is-4by3">
                <img src={IMAGES.CAPYBARA4} alt="Capybara relaxing" />
              </figure>
            </div>
            <div className="card-content">
              <p className="title is-4">
                <span className="icon-text">
                  <span className="icon has-text-info">
                    <FontAwesomeIcon icon="graduation-cap" />
                  </span>
                  <span>Ресурсы</span>
                </span>
              </p>
              <p className="subtitle is-6">Учебные материалы</p>
              <div className="content">
                Получите доступ к учебным материалам и ресурсам для успешного обучения в нашем институте.
              </div>
            </div>
          </div>
        </div>
      </div>

      {flag && (
        <div className="notification is-warning mt-4">
          <p className="has-text-weight-bold">Получен флаг:</p>
          <p className="is-family-monospace">{flag}</p>
        </div>
      )}
    </div>
  );
};

export default Home;
