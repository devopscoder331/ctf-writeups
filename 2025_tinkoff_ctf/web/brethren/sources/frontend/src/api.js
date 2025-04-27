import { getToken, clearToken, redirectToLogin, setToken } from './utils/auth';

const API_BASE = "/api/";

export const handleUnauthorized = (response) => {
  if (response.status === 401) {
    clearToken();
    redirectToLogin();
    throw new Error("Session expired. Please login again.");
  }
  return response;
};

export const fetchWithAuth = async (url, options = {}) => {
  const token = getToken();
  
  const headers = {
    ...options.headers,
    "X-API-KEY": token
  };
  
  const response = await fetch(url, { ...options, headers });
  
  handleUnauthorized(response);
  
  if (!response.ok) {
    const data = await response.json().catch(() => ({}));
    throw new Error(data.detail || "API request failed");
  }
  
  return response.json();
};

export async function loginUser(username, password) {
  const response = await fetch(`${API_BASE}auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });

  const data = await response.json();
  if (!response.ok) throw new Error(data.detail || "Login failed");
  setToken(data.token);
  return data.token;
}

export async function registerUser(userData) {
  const response = await fetch(`${API_BASE}auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(userData),
  });
  const data = await response.json();
  if (!response.ok) throw new Error(data.detail);
}

export async function fetchCourses() {
  return fetchWithAuth(`${API_BASE}course`);
}

export async function fetchUserProfile() {
  return fetchWithAuth(`${API_BASE}user/profile`);
}

export async function updateUserProfile(profileData) {
  return fetchWithAuth(`${API_BASE}user/profile`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(profileData)
  });
}

export async function rollCourse(courseId) {
  return fetchWithAuth(`${API_BASE}course/${courseId}/roll`, {
    method: "GET"
  });
}

export async function getTicketDetails(courseId, ticketId) {
  return fetchWithAuth(`${API_BASE}ticket?course_id=${courseId}&ticket_id=${ticketId}`);
}

export async function fetchCourseTickets(courseId) {
  try {
    const data = await fetchWithAuth(`${API_BASE}course/${courseId}/tickets`);
    return {
      course: data.course,
      tickets: data.tickets || []
    };
  } catch (error) {
    if (error.message === "Tickets not found") {
      return {
        course: { name: "Course" },
        tickets: []
      };
    }
    throw error;
  }
}

export async function fetchEnrolledCourses() {
  return fetchWithAuth(`${API_BASE}user/courses`);
}

export async function logoutUser() {
  try {
    await fetchWithAuth(`${API_BASE}auth/logout`, { method: "POST" });
  } finally {
    clearToken();
    redirectToLogin();
  }
}

export async function fetchUserInfo() {
  return fetchWithAuth(`${API_BASE}user/info`);
}

export async function getFlag() {
  return fetchWithAuth(`${API_BASE}flag/obtain`);
}
