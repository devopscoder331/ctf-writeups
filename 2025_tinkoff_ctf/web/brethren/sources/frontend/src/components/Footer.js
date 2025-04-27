import React from "react";
import { IMAGES } from "../utils/imageUtils";

const Footer = () => {
  return (
    <footer className="footer mt-6" style={{ backgroundColor: 'rgba(245, 239, 224, 0.7)' }}>
      <div className="content has-text-centered">
        <p className="is-size-5 has-text-weight-bold has-text-dark">
          Институт капибар и забавных наук — сводим вместе капибар и знания.
        </p>
        <p className="is-size-6 has-text-dark">
          <span role="img" aria-label="capybara">🦫</span> С любовью к капибарам с 2025 года <span role="img" aria-label="capybara">🦫</span>
        </p>
        <div className="level mt-4">
          <div className="level-item">
            <figure className="image is-48x48">
              <img src={IMAGES.CAPYBARA1} alt="Capybara" />
            </figure>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer; 
