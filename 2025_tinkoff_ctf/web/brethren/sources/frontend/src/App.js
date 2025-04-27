import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import "bulma/css/bulma.min.css";
import "./capybara-theme.css";
import Home from "./pages/Home";
import LoginPage from "./pages/Login";
import RegisterPage from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import CourseTickets from "./pages/CourseTickets";
import AdminPage from "./pages/Admin";
import ProtectedRoute from "./components/ProtectedRoute";
import AdminRoute from "./components/AdminRoute";
import Navbar from "./components/Navbar";
import Footer from "./components/Footer";
import BackgroundSetter from "./background";
import { AuthProvider } from "./utils/AuthContext";
import { ModalProvider } from './context/ModalContext';

import { library } from '@fortawesome/fontawesome-svg-core';
import { fas } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

library.add(fas);

const AppContent = () => {
  return (
    <div className="app">
      <BackgroundSetter />
      <Navbar />
      <main>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/course/:id/tickets"
            element={
              <ProtectedRoute>
                <CourseTickets />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin"
            element={
              <AdminRoute>
                <AdminPage />
              </AdminRoute>
            }
          />
        </Routes>
      </main>
      <Footer />
    </div>
  );
};

const App = () => {
  return (
    <Router>
      <AuthProvider>
        <ModalProvider>
          <AppContent />
        </ModalProvider>
      </AuthProvider>
    </Router>
  );
};

export default App;
