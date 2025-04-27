import React, { createContext, useContext, useState, useCallback } from 'react';

const ModalContext = createContext();

export const ModalProvider = ({ children }) => {
  const [modalContent, setModalContent] = useState(null);
  const [modalTitle, setModalTitle] = useState('');
  const [isOpen, setIsOpen] = useState(false);

  const openModal = useCallback((title, content) => {
    setModalTitle(title);
    setModalContent(content);
    setIsOpen(true);
    document.body.style.overflow = 'hidden';
  }, []);

  const closeModal = useCallback(() => {
    setIsOpen(false);
    setModalContent(null);
    setModalTitle('');
    document.body.style.overflow = 'unset';
  }, []);

  return (
    <ModalContext.Provider value={{ openModal, closeModal }}>
      {children}
      {isOpen && (
        <div className="modal is-active">
          <div className="modal-background" onClick={closeModal}></div>
          <div className="modal-card">
            <header className="modal-card-head">
              <p className="modal-card-title">{modalTitle}</p>
              <button 
                className="delete" 
                aria-label="close" 
                onClick={closeModal}
              ></button>
            </header>
            <section className="modal-card-body">
              <div className="content">
                {modalContent}
              </div>
            </section>
            <footer className="modal-card-foot">
              <button className="button" onClick={closeModal}>Закрыть</button>
            </footer>
          </div>
        </div>
      )}
    </ModalContext.Provider>
  );
};

export const useModal = () => {
  const context = useContext(ModalContext);
  if (!context) {
    throw new Error('useModal must be used within a ModalProvider');
  }
  return context;
}; 