export const isAuthenticated = () => {
  const token = localStorage.getItem("token");
  return !!token;
};

export const getToken = () => {
  return localStorage.getItem("token");
};

export const setToken = (token) => {
  localStorage.setItem("token", token);
  window.dispatchEvent(new StorageEvent('storage', {
    key: 'token',
    newValue: token
  }));
};

export const clearToken = () => {
  localStorage.removeItem("token");
};

export const redirectToLogin = () => {
  window.location.href = "/";
};

export const redirectToDashboard = () => {
  window.location.href = "/dashboard";
}; 