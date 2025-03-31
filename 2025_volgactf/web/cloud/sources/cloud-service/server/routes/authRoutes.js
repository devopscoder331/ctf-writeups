const express = require('express');
const router = express.Router();
const authController = require('../controllers/authController');
const { isAuthenticated } = require('../middleware/auth');
const { validateRegistration, validateLogin } = require('../middleware/validators');

router.post('/register', validateRegistration, authController.register);
router.post('/login', validateLogin, authController.login);
router.get('/logout', isAuthenticated, authController.logout);
router.get('/me', isAuthenticated, authController.getCurrentUser);

module.exports = router; 