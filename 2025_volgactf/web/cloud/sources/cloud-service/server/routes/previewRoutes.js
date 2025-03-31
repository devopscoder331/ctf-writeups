const express = require('express');
const router = express.Router();
const fileController = require('../controllers/fileController');

router.get(
    '/shared/:token',
    fileController.getSharedFile
);

module.exports = router; 