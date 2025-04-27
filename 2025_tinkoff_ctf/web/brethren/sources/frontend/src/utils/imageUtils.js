export const getImagePath = (imageName) => {
  return `${process.env.PUBLIC_URL}/images/${imageName}`;
};

export const IMAGES = {
  CAPYBARA1: getImagePath('capybara1.png'),
  CAPYBARA2: getImagePath('capybara2.jpg'),
  CAPYBARA3: getImagePath('capybara3.jpg'),
  CAPYBARA4: getImagePath('capybara4.jpg'),
}; 