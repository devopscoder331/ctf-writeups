import React, { useEffect } from 'react';

const BackgroundSetter = () => {
  useEffect(() => {
    document.body.style.backgroundImage = `url(${process.env.PUBLIC_URL}/images/capybara2.png)`;
  }, []);

  return null;
};

export default BackgroundSetter; 