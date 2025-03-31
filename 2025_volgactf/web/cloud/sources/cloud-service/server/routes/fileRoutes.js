const express = require('express');
const router = express.Router();
const fileController = require('../controllers/fileController');
const { isAuthenticated } = require('../middleware/auth');
const upload = require('../middleware/upload');
const { validateFileUpload, validateFolderCreation } = require('../middleware/validators');

router.post(
  '/upload', 
  isAuthenticated, 
  upload.single('file'), 
  validateFileUpload,
  fileController.uploadFile
);

router.post(
  '/folder', 
  isAuthenticated, 
  validateFolderCreation,
  fileController.createFolder
);

router.get(
  '/', 
  isAuthenticated, 
  fileController.getItems
);

router.delete(
  '/:type/:id', 
  isAuthenticated, 
  fileController.deleteItem
);

router.post(
  '/share/:id',
  isAuthenticated,
  fileController.shareFile
);

router.delete(
  '/share/:id',
  isAuthenticated,
  fileController.revokeShareLink
);

module.exports = router; 