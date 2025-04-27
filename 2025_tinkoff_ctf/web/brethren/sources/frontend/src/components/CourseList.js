import React, { useState, useEffect } from "react";
import { fetchCourses, rollCourse } from "../api";
import { useNavigate } from "react-router-dom";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useModal } from '../context/ModalContext';

const CourseList = () => {
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [ticketLoading, setTicketLoading] = useState(false);
  const navigate = useNavigate();
  const { openModal } = useModal();

  useEffect(() => {
    const loadCourses = async () => {
      try {
        setLoading(true);
        const data = await fetchCourses();
        setCourses(data);
      } catch (err) {
        setError(err.message);
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    
    loadCourses();
  }, []);

  const handleRoll = async (courseId, e) => {
    e.stopPropagation();
    try {
      setTicketLoading(true);
      const ticket = await rollCourse(courseId);
      
      openModal('Случайный билет', (
        <p style={{ whiteSpace: 'pre-wrap' }}>{ticket.text}</p>
      ));

      const updatedCourses = await fetchCourses();
      setCourses(updatedCourses);
    } catch (err) {
      setError(err.message);
    } finally {
      setTicketLoading(false);
    }
  };

  const handleViewTickets = (courseId) => {
    console.log('Navigating to tickets for course:', courseId);
    navigate(`/course/${courseId}/tickets`);
  };

  if (loading) return <div className="has-text-centered py-5"><p>Загрузка курсов...</p></div>;
  if (error) return <div className="notification is-danger">{error}</div>;

  return (
    <div className="columns is-multiline">
      {courses.length === 0 ? (
        <div className="column is-12">
          <div className="notification is-info">
            Курсы в данный момент не доступны.
          </div>
        </div>
      ) : (
        courses.map((course) => (
          <div key={course.id} className="column is-4">
            <div 
              className="card course-card" 
              onClick={() => handleViewTickets(course.id)}
              style={{ 
                cursor: 'pointer', 
                height: '100%',
                display: 'flex',
                flexDirection: 'column'
              }}
            >
              <div className="card-content" style={{ flex: '1 1 auto' }}>
                <p className="title is-4">{course.name}</p>
                <p className="subtitle is-6">{course.code}</p>
                <div className="content">
                  {course.description}
                </div>
              </div>
              
              <footer className="card-footer" style={{ marginTop: 'auto' }}>
                <div className="card-footer-item">
                  <button 
                    className={`button is-primary ${course.enrolled ? 'is-light' : ''} ${ticketLoading ? 'is-loading' : ''}`}
                    onClick={(e) => handleRoll(course.id, e)}
                    disabled={course.enrolled}
                  >
                    <span className="icon">
                      <FontAwesomeIcon icon={course.enrolled ? "check" : "plus"} />
                    </span>
                    <span>{"Случайный билет"}</span>
                  </button>
                </div>
                <div className="card-footer-item">
                  <button 
                    className="button is-info"
                    onClick={(e) => {
                      e.stopPropagation();
                      handleViewTickets(course.id);
                    }}
                  >
                    <span className="icon">
                      <FontAwesomeIcon icon="ticket-alt" />
                    </span>
                    <span>Билеты</span>
                  </button>
                </div>
              </footer>
            </div>
          </div>
        ))
      )}
    </div>
  );
};

export default CourseList;
