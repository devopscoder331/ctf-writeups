import React from "react";
import CourseList from "../components/CourseList";
import { IMAGES } from "../utils/imageUtils";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const Dashboard = () => {
  return (
    <div className="container">
      <div className="box mt-5 p-5">
        <div className="columns is-vcentered mb-4">
          <div className="column">
            <h1 className="title is-3">
              <span className="icon-text">
                <span className="icon has-text-primary">
                  <FontAwesomeIcon icon="ticket-alt" />
                </span>
                <span>Доступные курсы</span>
              </span>
            </h1>
            <p className="subtitle is-5">Выберите курс для получения билетов</p>
          </div>
          <div className="column is-narrow">
            <img 
              src={IMAGES.CAPYBARA4} 
              alt="Capybara" 
              style={{ width: '80px', height: '80px', borderRadius: '50%' }}
            />
          </div>
        </div>
        <CourseList />
      </div>
    </div>
  );
};

export default Dashboard;
