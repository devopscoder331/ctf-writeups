import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { fetchCourseTickets, getTicketDetails } from '../api';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { IMAGES } from '../utils/imageUtils';
import { useModal } from '../context/ModalContext';
import { formatTicketTitle } from '../utils/formatUtils';

const CourseTickets = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [courseName, setCourseName] = useState('');
  const [ticketDetailsLoading, setTicketDetailsLoading] = useState(false);
  const { openModal } = useModal();

  useEffect(() => {
    const loadTickets = async () => {
      try {
        setLoading(true);
        console.log('Loading tickets for course:', id);
        const data = await fetchCourseTickets(id);
        console.log('Received data:', data);
        setTickets(Array.isArray(data.tickets) ? data.tickets : []);
        setCourseName(data.course?.name || 'Course');
      } catch (err) {
        console.error('Error loading tickets:', err);
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      loadTickets();
    } else {
      console.error('No course ID provided');
      setError('No course ID provided');
      setLoading(false);
    }
  }, [id]);

  const handleBack = () => {
    navigate('/dashboard');
  };

  const handleViewTicketDetails = async (ticketId) => {
    try {
      setTicketDetailsLoading(true);
      const ticketDetails = await getTicketDetails(id, ticketId);
      
      openModal('Детали билета', (
        <>
          <h4>{formatTicketTitle(ticketDetails.title)}</h4>
          <p style={{ whiteSpace: 'pre-wrap' }}>{ticketDetails.text}</p>
        </>
      ));
    } catch (err) {
      setError(err.message);
    } finally {
      setTicketDetailsLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="container mt-5">
        <div className="has-text-centered py-5">
          <p>Загрузка билетов...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container mt-5">
        <div className="notification is-danger">{error}</div>
        <button className="button is-primary" onClick={handleBack}>
          Назад к курсам
        </button>
      </div>
    );
  }

  return (
    <div className="container">
      <div className="box mt-5 p-5">
        <div className="columns is-vcentered mb-4">
          <div className="column">
            <button className="button is-small mb-3" onClick={handleBack}>
              <span className="icon">
                <FontAwesomeIcon icon="arrow-left" />
              </span>
              <span>Назад к курсам</span>
            </button>
            <h1 className="title is-3">
              <span className="icon-text">
                <span className="icon has-text-info">
                  <FontAwesomeIcon icon="ticket-alt" />
                </span>
                <span>{courseName} — билеты</span>
              </span>
            </h1>
            <p className="subtitle is-5">Доступные билеты для этого курса</p>
          </div>
          <div className="column is-narrow">
            <img 
              src={IMAGES.CAPYBARA3} 
              alt="Capybara" 
              style={{ width: '80px', height: '80px', borderRadius: '50%' }}
            />
          </div>
        </div>

        {tickets.length === 0 ? (
          <div className="notification is-info">
            Билетов для этого курса ещё нет.
          </div>
        ) : (
          <div className="columns is-multiline">
            {tickets.map((ticket) => (
              <div key={ticket.id} className="column is-4">
                <div className="card" style={{ 
                  height: '100%',
                  display: 'flex',
                  flexDirection: 'column'
                }}>
                  <div className="card-content" style={{ flex: '1 1 auto' }}>
                    <p className="title is-4">{formatTicketTitle(ticket.title || ticket)}</p>
                    {ticket.description && (
                      <div className="content">
                        <p>{ticket.description}</p>
                      </div>
                    )}
                  </div>
                  <footer className="card-footer" style={{ marginTop: 'auto' }}>
                    <div className="card-footer-item">
                      <button 
                        className={`button is-primary is-fullwidth ${ticketDetailsLoading ? 'is-loading' : ''}`}
                        onClick={() => handleViewTicketDetails(ticket)}
                      >
                        <span className="icon">
                          <FontAwesomeIcon icon="eye" />
                        </span>
                        <span>Посмотреть детали</span>
                      </button>
                    </div>
                  </footer>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default CourseTickets; 
