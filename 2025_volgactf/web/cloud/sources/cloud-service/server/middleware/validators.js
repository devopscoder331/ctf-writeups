const { body, validationResult } = require('express-validator');
const path = require('path');

// Middleware to validate registration inputs
const validateRegistration = [
  // Name validation
  body('name')
    .trim()
    .notEmpty().withMessage('Name is required')
    .isLength({ min: 2, max: 50 }).withMessage('Name must be between 2 and 50 characters')
    .matches(/^[a-zA-Z0-9\s]+$/).withMessage('Name can only contain letters, numbers and spaces'),
  
  // Email validation
  body('email')
    .trim()
    .notEmpty().withMessage('Email is required')
    .isEmail().withMessage('Please provide a valid email address')
    .normalizeEmail(),
  
  // Password validation
  body('password')
    .trim()
    .notEmpty().withMessage('Password is required')
    .isLength({ min: 6 }).withMessage('Password must be at least 6 characters long')
    .matches(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/).withMessage('Password must contain at least one uppercase letter, one lowercase letter, and one number'),
  
  // Validation result middleware
  (req, res, next) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }
    next();
  }
];

// Middleware to validate login inputs
const validateLogin = [
  // Email validation
  body('email')
    .trim()
    .notEmpty().withMessage('Email is required')
    .isEmail().withMessage('Please provide a valid email address')
    .normalizeEmail(),
  
  // Password validation
  body('password')
    .trim()
    .notEmpty().withMessage('Password is required'),
  
  // Validation result middleware
  (req, res, next) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }
    next();
  }
];

// Middleware to validate folder creation
const validateFolderCreation = [
  body('name')
    .trim()
    .notEmpty().withMessage('Folder name is required')
    .isLength({ min: 1, max: 100 }).withMessage('Folder name must be between 1 and 100 characters')
    .matches(/^[a-zA-Z0-9\s_\-\.]+$/).withMessage('Folder name can only contain letters, numbers, spaces, underscores, hyphens, and periods'),
  
  // Validation result middleware
  (req, res, next) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }
    next();
  }
];

// Middleware to validate file uploads
const validateFileUpload = (req, res, next) => {
  // Check if a file was uploaded
  if (!req.file) {
    return res.status(400).json({ message: 'No file uploaded' });
  }
  
  // Get file extension
  const ext = path.extname(req.file.originalname).toLowerCase();
  
  // Define allowed extensions
  // You can modify this list based on what file types you want to support
  const allowedExtensions = [
    '.jpg', '.jpeg', '.png', '.gif', '.pdf', 
    '.doc', '.docx', '.xls', '.xlsx', '.ppt', '.pptx',
    '.txt', '.csv', '.html', '.htm', '.mp4', '.mp3'
  ];
  
  // Check if the file extension is allowed
  if (!allowedExtensions.includes(ext)) {
    // Delete the file if it's not an allowed type
    const fs = require('fs');
    fs.unlinkSync(req.file.path);
    
    return res.status(400).json({ 
      message: 'Invalid file type. Allowed types: ' + allowedExtensions.join(', ') 
    });
  }
  
  // File size validation (already handled by multer, but could be enhanced here)
  
  next();
};

module.exports = {
  validateRegistration,
  validateLogin,
  validateFolderCreation,
  validateFileUpload
}; 